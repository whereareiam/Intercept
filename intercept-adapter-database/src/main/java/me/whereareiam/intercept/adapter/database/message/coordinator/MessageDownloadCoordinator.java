package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentInterception;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MessageDownloadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageRegexPatternRepository patternRepository;
	private final MessageRegexPlaceholderRepository placeholderRepository;
	private final MessageFileWriter fileWriter;

	public MessageSnapshot download() {
		List<MessageFileEntity> files = fileRepository.findAll();
		if (files.isEmpty()) return new MessageSnapshot(Map.of(), Map.of());

		Map<String, TranslationEntry> entrySnapshot = new LinkedHashMap<>();
		Map<String, Path> fileSnapshot = new LinkedHashMap<>();

		for (MessageFileEntity fileEntity : files) {
			AssemblyResult result = assemble(fileEntity);
			fileWriter.write(fileEntity.getFilePath(), result.fileData());
			fileSnapshot.put(fileEntity.getKeyPrefix(), fileWriter.resolvePath(fileEntity.getFilePath()));
			entrySnapshot.putAll(result.snapshotEntries());
		}

		return new MessageSnapshot(entrySnapshot, fileSnapshot);
	}

	private AssemblyResult assemble(MessageFileEntity fileEntity) {
		List<MessageEntryEntity> entryEntities = entryRepository.findAllByFileId(fileEntity.getId());
		Map<String, Object> fileItems = new LinkedHashMap<>();
		Map<String, TranslationEntry> snapshotEntries = new LinkedHashMap<>();

		for (MessageEntryEntity entryEntity : entryEntities) {
			EntryAssembly entryAssembly = assembleEntry(fileEntity.getKeyPrefix(), entryEntity);
			fileItems.put(entryEntity.getEntryKey(), entryAssembly.fileData());
			snapshotEntries.put(entryAssembly.fullKey(), entryAssembly.snapshotEntry());
		}

		MessageDocument fileData = new MessageDocument();
		fileItems.forEach(fileData::putEntry);

		return new AssemblyResult(fileData, snapshotEntries);
	}

	private EntryAssembly assembleEntry(String keyPrefix, MessageEntryEntity entryEntity) {
		String entryKey = entryEntity.getEntryKey();
		String fullKey = buildFullKey(keyPrefix, entryKey);
		MessageType entryType = entryEntity.getEntryType();

		TranslationAssembly translations = loadTranslations(entryEntity.getId());

		MessageDocumentEntry fileData = new MessageDocumentEntry();
		Map<String, MultiValue<String>> locales = toDocumentLocales(translations);
		if (locales != null && !locales.isEmpty()) {
			fileData.setLocales(locales);
		} else if (translations.defaultText() != null) {
			fileData.setText(MultiValue.of(translations.defaultText()));
		}

		MessageDocumentInterception interception = assembleRegex(entryEntity.getId());
		if (interception != null && interception.getPatterns() != null && !interception.getPatterns().isEmpty()) {
			fileData.setInterception(interception);
		}

		TranslationEntry snapshotEntry = buildSnapshotEntry(entryType, translations);

		return new EntryAssembly(fullKey, snapshotEntry, fileData, entryType);
	}

	private TranslationAssembly loadTranslations(long entryId) {
		List<MessageTranslationEntity> translationEntities = translationRepository.findAllByEntryId(entryId);
		if (translationEntities.isEmpty()) return new TranslationAssembly(null, null);

		String defaultText = null;
		Map<Locale, String> translations = new LinkedHashMap<>();

		for (MessageTranslationEntity translation : translationEntities) {
			Locale locale = translation.getLocale();
			if (locale == null) {
				defaultText = translation.getText();
				continue;
			}

			translations.put(locale, translation.getText());
		}

		return new TranslationAssembly(defaultText, translations.isEmpty() ? null : translations);
	}

	private Map<String, MultiValue<String>> toDocumentLocales(TranslationAssembly translations) {
		if ((translations.defaultText() == null || translations.defaultText().isEmpty())
				&& (translations.translations() == null || translations.translations().isEmpty())) {
			return null;
		}

		Map<String, MultiValue<String>> documentLocales = new LinkedHashMap<>();
		if (translations.defaultText() != null) {
			documentLocales.put("default", MultiValue.of(translations.defaultText()));
		}

		if (translations.translations() != null) {
			for (Map.Entry<Locale, String> entry : translations.translations().entrySet()) {
				Locale locale = entry.getKey();
				String localeKey = locale != null ? LocaleUtil.formatLocale(locale) : "default";
				documentLocales.put(localeKey, MultiValue.of(entry.getValue()));
			}
		}

		return documentLocales;
	}

	private MessageDocumentInterception assembleRegex(long entryId) {
		List<MessageRegexPatternEntity> patternEntities = patternRepository.findAllByEntryId(entryId);
		if (patternEntities.isEmpty()) return null;

		List<MessageDocumentRegex> fileRegex = new ArrayList<>();

		for (MessageRegexPatternEntity patternEntity : patternEntities) {
			Map<String, String> placeholders = placeholderRepository.findAllByPatternId(patternEntity.getId())
					.stream()
					.collect(Collectors.toMap(
							MessageRegexPlaceholderEntity::getPlaceholderName,
							MessageRegexPlaceholderEntity::getCaptureGroup,
							(a, b) -> b,
							LinkedHashMap::new
					));

			MessageDocumentRegex fileRegexEntry = new MessageDocumentRegex();
			fileRegexEntry.setPattern(patternEntity.getPattern());
			fileRegexEntry.setPriority(patternEntity.getPriority());
			fileRegexEntry.setReplaceMatched(patternEntity.isReplaceMatched());
			if (!placeholders.isEmpty()) {
				fileRegexEntry.setPlaceholders(placeholders);
			}
			fileRegex.add(fileRegexEntry);
		}

		MessageDocumentInterception interception = new MessageDocumentInterception();
		interception.setPatterns(fileRegex);
		return interception;
	}

	private String buildFullKey(String keyPrefix, String entryKey) {
		return keyPrefix == null || keyPrefix.isBlank() ? entryKey : keyPrefix + "." + entryKey;
	}

	private TranslationEntry buildSnapshotEntry(
			MessageType entryType, TranslationAssembly translations
	) {
		if (translations.translations() != null && !translations.translations().isEmpty()) {
			Map<TranslationLocale, String> map = new LinkedHashMap<>();
			for (Map.Entry<Locale, String> entry : translations.translations().entrySet()) {
				Locale locale = entry.getKey();
				map.put(SemanticLocale.wrap(locale), entry.getValue());
			}
			return new LocalizedEntry(map);
		}

		String text = translations.defaultText();
		return text != null ? new TemplateEntry(text) : null;
	}

	private record AssemblyResult(MessageDocument fileData, Map<String, TranslationEntry> snapshotEntries) {}

	private record EntryAssembly(String fullKey, TranslationEntry snapshotEntry, MessageDocumentEntry fileData,
	                             MessageType entryType) {}

	private record TranslationAssembly(String defaultText, Map<Locale, String> translations) {}
}
