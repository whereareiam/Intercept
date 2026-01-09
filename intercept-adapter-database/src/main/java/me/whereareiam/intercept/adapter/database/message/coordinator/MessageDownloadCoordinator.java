package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageExtensionEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageTranslationEntity;
import me.whereareiam.intercept.adapter.database.message.MessageExtensionCodec;
import me.whereareiam.intercept.adapter.database.repository.message.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageExtensionRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTemplateRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTranslationRepository;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.nio.file.Path;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MessageDownloadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageTemplateRepository templateRepository;
	private final MessageExtensionRepository extensionRepository;
	private final MessageFileWriter fileWriter;

	public MessageSnapshot download() {
		List<MessageFileEntity> files = fileRepository.findAllByNamespace(Constants.Namespace.INTERNAL);
		if (files.isEmpty()) return new MessageSnapshot(Map.of(), Map.of());

		Map<String, TranslationEntry> entrySnapshot = new LinkedHashMap<>();
		Map<String, Path> fileSnapshot = new LinkedHashMap<>();
		Map<String, MessageExtensions> extensionSnapshot = new LinkedHashMap<>();

		for (MessageFileEntity fileEntity : files) {
			AssemblyResult result = assemble(fileEntity);
			fileWriter.write(fileEntity.getNamespace(), fileEntity.getFilePath(), result.fileData());
			fileSnapshot.put(
					fileEntity.getKeyPrefix(),
					fileWriter.resolvePath(fileEntity.getNamespace(), fileEntity.getFilePath())
			);

			entrySnapshot.putAll(result.snapshotEntries());
			extensionSnapshot.putAll(result.extensionEntries());
		}

		return new MessageSnapshot(entrySnapshot, fileSnapshot, extensionSnapshot);
	}

	private AssemblyResult assemble(MessageFileEntity fileEntity) {
		List<MessageEntryEntity> entryEntities = entryRepository.findAllByFileId(fileEntity.getId());
		Map<String, MessageFileData.Node> fileItems = new LinkedHashMap<>();
		Map<String, TranslationEntry> snapshotEntries = new LinkedHashMap<>();
		Map<String, MessageExtensions> extensionEntries = new LinkedHashMap<>();

		for (MessageEntryEntity entryEntity : entryEntities) {
			EntryAssembly entryAssembly = assembleEntry(fileEntity.getKeyPrefix(), entryEntity);
			putFileEntry(fileItems, entryAssembly.entrySegments(), entryAssembly.fileData());
			snapshotEntries.put(entryAssembly.fullKey(), entryAssembly.snapshotEntry());
			if (entryAssembly.extensions() != null && !entryAssembly.extensions().isEmpty())
				extensionEntries.put(entryAssembly.fullKey(), entryAssembly.extensions());
		}

		MessageFileData fileData = new MessageFileData();
		fileItems.forEach(fileData::putEntry);

		return new AssemblyResult(fileData, snapshotEntries, extensionEntries);
	}

	private EntryAssembly assembleEntry(String keyPrefix, MessageEntryEntity entryEntity) {
		String entryKeyRaw = entryEntity.getEntryKeyRaw();
		if (entryKeyRaw == null || entryKeyRaw.isBlank())
			entryKeyRaw = entryEntity.getEntryKey();

		List<String> entrySegments = splitEntryKey(entryKeyRaw);
		String canonicalEntryKey = joinEntryKey(entrySegments);
		String fullKey = buildFullKey(keyPrefix, canonicalEntryKey);
		MessageType entryType = entryEntity.getEntryType();

		TranslationAssembly translations = loadTranslations(entryEntity.getId());
		String templateText = loadTemplateText(entryEntity.getId());

		MessageFileData.Entry fileData = new MessageFileData.Entry();
		if (entryType == MessageType.TEMPLATE) {
			if (templateText != null) {
				fileData.setText(MessageValue.text(templateText));
			}
		} else {
			Map<String, MessageValue> locales = toDocumentLocales(translations.translations());
			if (locales != null && !locales.isEmpty())
				fileData.setLocales(locales);
		}

		MessageExtensions extensions = loadExtensions(entryEntity.getId());
		if (extensions != null && !extensions.isEmpty())
			fileData.setExtensions(extensions);

		TranslationEntry snapshotEntry = buildSnapshotEntry(
				entryType,
				translations.translations(),
				templateText
		);

		return new EntryAssembly(fullKey, snapshotEntry, fileData, entryType, extensions, entrySegments);
	}

	private TranslationAssembly loadTranslations(long entryId) {
		List<MessageTranslationEntity> translationEntities = translationRepository.findAllByEntryId(entryId);
		if (translationEntities.isEmpty()) return new TranslationAssembly(null);

		Map<Locale, String> translations = new LinkedHashMap<>();

		for (MessageTranslationEntity translation : translationEntities) {
			Locale locale = translation.getLocale();
			if (locale == null) {
				continue;
			}

			translations.put(locale, translation.getText());
		}

		return new TranslationAssembly(translations.isEmpty() ? null : translations);
	}

	private String loadTemplateText(long entryId) {
		if (templateRepository == null) return null;
		var template = templateRepository.findByEntryId(entryId);

		return template == null
				? null
				: template.getText();
	}

	private Map<String, MessageValue> toDocumentLocales(Map<Locale, String> translations) {
		if (translations == null || translations.isEmpty())
			return null;

		Map<String, MessageValue> documentLocales = new LinkedHashMap<>();
		for (Map.Entry<Locale, String> entry : translations.entrySet()) {
			Locale locale = entry.getKey();
			String localeKey = locale != null ? LocaleUtil.formatLocale(locale) : "default";
			documentLocales.put(localeKey, MessageValue.text(entry.getValue()));
		}

		return documentLocales;
	}

	private MessageExtensions loadExtensions(long entryId) {
		if (extensionRepository == null) return null;
		List<MessageExtensionEntity> entities = extensionRepository.findAllByEntryId(entryId);
		if (entities == null || entities.isEmpty()) return null;

		MessageExtensions extensions = new MessageExtensions();
		for (MessageExtensionEntity entity : entities) {
			String extensionId = entity.getExtensionId();
			if (extensionId == null || extensionId.isBlank()) continue;
			Map<String, Object> data = MessageExtensionCodec.decode(entity.getPayload());
			MapMessageExtensionPayload payload = new MapMessageExtensionPayload(extensionId, data);
			extensions.put(new MessageExtensionKey<>(extensionId, MapMessageExtensionPayload.class), payload);
		}

		return extensions.isEmpty() ? null : extensions;
	}

	private String buildFullKey(String keyPrefix, String entryKey) {
		if (keyPrefix == null || keyPrefix.isBlank()) return entryKey;
		if (entryKey == null || entryKey.isBlank()) return keyPrefix;
		if (!keyPrefix.endsWith(Constants.Namespace.NAMESPACE_SEPARATOR) && keyPrefix.endsWith("." + entryKey)) return keyPrefix;
		if (keyPrefix.endsWith(Constants.Namespace.NAMESPACE_SEPARATOR)) return keyPrefix + entryKey;

		return keyPrefix + "." + entryKey;
	}

	private void putFileEntry(
			Map<String, MessageFileData.Node> root,
			List<String> entrySegments,
			MessageFileData.Entry entry
	) {
		if (root == null || entrySegments == null || entrySegments.isEmpty() || entry == null) return;
		Map<String, MessageFileData.Node> current = root;

		for (int i = 0; i < entrySegments.size(); i++) {
			String part = entrySegments.get(i);
			if (part.isEmpty()) continue;

			if (i == entrySegments.size() - 1) {
				current.put(part, entry);
				return;
			}

			MessageFileData.Node existing = current.get(part);
			MessageFileData.Section section;
			if (existing instanceof MessageFileData.Section existingSection) {
				section = existingSection;
			} else {
				section = new MessageFileData.Section();
				current.put(part, section);
			}

			current = section.getEntries();
		}
	}

	private List<String> splitEntryKey(String entryKey) {
		if (entryKey == null || entryKey.isBlank()) return List.of();
		List<String> segments = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean escape = false;

		for (int i = 0; i < entryKey.length(); i++) {
			char c = entryKey.charAt(i);
			if (escape) {
				current.append(c);
				escape = false;
				continue;
			}

			if (c == '\\') {
				escape = true;
				continue;
			}

			if (c == '.') {
				segments.add(current.toString());
				current.setLength(0);
				continue;
			}

			current.append(c);
		}

		segments.add(current.toString());
		return segments;
	}

	private String joinEntryKey(List<String> segments) {
		if (segments == null || segments.isEmpty()) return "";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < segments.size(); i++) {
			if (i > 0) builder.append('.');
			builder.append(segments.get(i));
		}

		return builder.toString();
	}

	private TranslationEntry buildSnapshotEntry(
			MessageType entryType,
			Map<Locale, String> translations,
			String templateText
	) {
		if (translations != null
				&& !translations.isEmpty()
				&& (entryType == null || entryType == MessageType.MESSAGE)) {
			Map<TranslationLocale, String> map = new LinkedHashMap<>();
			for (Map.Entry<Locale, String> entry : translations.entrySet()) {
				Locale locale = entry.getKey();
				map.put(SemanticLocale.wrap(locale), entry.getValue());
			}
			return new LocalizedEntry(map);
		}

		return templateText != null ? new TemplateEntry(templateText) : null;
	}

	private record AssemblyResult(
			MessageFileData fileData,
			Map<String, TranslationEntry> snapshotEntries,
			Map<String, MessageExtensions> extensionEntries
	) {}

	private record EntryAssembly(
			String fullKey,
			TranslationEntry snapshotEntry,
			MessageFileData.Entry fileData,
			MessageType entryType,
			MessageExtensions extensions,
			List<String> entrySegments
	) {}

	private record TranslationAssembly(Map<Locale, String> translations) {}
}
