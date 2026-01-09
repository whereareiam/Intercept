package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.message.MessageExtensionCodec;
import me.whereareiam.intercept.adapter.database.message.MessageKeyResolver;
import me.whereareiam.intercept.adapter.database.repository.message.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageExtensionRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTemplateRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTranslationRepository;
import me.whereareiam.intercept.util.NamespaceUtil;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MessageUploadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageTemplateRepository templateRepository;
	private final MessageExtensionRepository extensionRepository;
	private final Path messagesPath;

	@Inject
	public MessageUploadCoordinator(
			MessageFileRepository fileRepository,
			MessageEntryRepository entryRepository,
			MessageTranslationRepository translationRepository,
			MessageTemplateRepository templateRepository,
			MessageExtensionRepository extensionRepository,
			@Named("messagesPath") Path messagesPath
	) {
		this.fileRepository = fileRepository;
		this.entryRepository = entryRepository;
		this.translationRepository = translationRepository;
		this.templateRepository = templateRepository;
		this.extensionRepository = extensionRepository;
		this.messagesPath = messagesPath;
	}

	public void upload(MessageSnapshot snapshot) {
		Set<String> namespaces = resolveNamespaces(snapshot.getFilePaths());
		for (String namespace : namespaces)
			fileRepository.deleteByNamespace(namespace);

		Map<String, MessageFileEntity> fileEntities = createFileEntities(snapshot.getFilePaths());
		processEntries(snapshot.getEntries(), fileEntities, snapshot.getExtensions());
	}

	private Map<String, MessageFileEntity> createFileEntities(Map<String, Path> filePaths) {
		Map<String, MessageFileEntity> fileEntities = new HashMap<>();

		for (Map.Entry<String, Path> fileEntry : filePaths.entrySet()) {
			String keyPrefix = fileEntry.getKey();
			String namespace = NamespaceUtil.getNamespace(keyPrefix);
			String rawPrefix = NamespaceUtil.stripNamespace(keyPrefix);
			if (namespace == null || namespace.isBlank()) {
				namespace = Constants.Namespace.INTERNAL;
				keyPrefix = NamespaceUtil.qualify(namespace, rawPrefix);
			}

			Path filePath = fileEntry.getValue();

			Path relativePath = messagesPath.relativize(filePath);
			String relativePathString = relativePath.toString().replace('\\', '/');

			int lastDot = relativePathString.lastIndexOf('.');
			if (lastDot > 0) relativePathString = relativePathString.substring(0, lastDot);

			MessageFileEntity newFile = new MessageFileEntity();
			newFile.setNamespace(namespace);
			newFile.setFilePath(relativePathString);
			MessageFileEntity savedFile = fileRepository.save(newFile);

			fileEntities.put(keyPrefix, savedFile);
		}

		return fileEntities;
	}

	private void processEntries(
			Map<String, TranslationEntry> entries,
			Map<String, MessageFileEntity> fileEntities,
			Map<String, MessageExtensions> extensions
	) {
		for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
			String fullKey = entry.getKey();
			if (!NamespaceUtil.hasNamespace(fullKey))
				fullKey = NamespaceUtil.qualify(Constants.Namespace.INTERNAL, fullKey);

			TranslationEntry messageEntry = entry.getValue();

			String keyPrefix = MessageKeyResolver.findKeyPrefix(fullKey, fileEntities.keySet());
			MessageFileEntity fileEntity = fileEntities.get(keyPrefix);

			if (fileEntity == null) continue;

			String entryKeyRaw = MessageKeyResolver.extractEntryKey(fullKey, keyPrefix);
			String entryKey = unescapeEntryKey(entryKeyRaw);

			MessageEntryEntity entryEntity = entryRepository.save(MessageEntryEntity.builder()
					.file(fileEntity)
					.entryKey(entryKey)
					.entryKeyRaw(entryKeyRaw)
					.entryType(resolveEntryType(messageEntry))
					.build());

			processTranslations(entryEntity, messageEntry);
			processExtensions(entryEntity, fullKey, extensions);
		}
	}

	private void processTranslations(MessageEntryEntity entryEntity, TranslationEntry messageEntry) {
		if (messageEntry instanceof LocalizedEntry localizedEntry) {
			if (translationRepository == null) return;

			Map<TranslationLocale, String> translations = localizedEntry.getTranslations();
			Map<Locale, String> resolved = toLocaleMap(translations);
			for (Map.Entry<Locale, String> translation : resolved.entrySet()) {
				translationRepository.insert(entryEntity.getId(), translation.getKey(), translation.getValue());
			}

			return;
		}

		if (messageEntry instanceof TemplateEntry templateEntry) {
			if (templateRepository == null) return;

			String text = templateEntry.getTemplate();
			if (text == null) return;

			templateRepository.insert(entryEntity.getId(), text);
		}
	}

	private void processExtensions(
			MessageEntryEntity entryEntity,
			String fullKey,
			Map<String, MessageExtensions> extensionData
	) {
		if (extensionRepository == null || extensionData == null || extensionData.isEmpty()) return;

		MessageExtensions extensions = extensionData.get(fullKey);
		if (extensions == null && NamespaceUtil.hasNamespace(fullKey))
			extensions = extensionData.get(NamespaceUtil.stripNamespace(fullKey));

		if (extensions == null || extensions.isEmpty()) return;

		for (MessageExtensionPayload payload : extensions.entries().values()) {
			if (!(payload instanceof MapMessageExtensionPayload mapPayload)) continue;
			String payloadData = MessageExtensionCodec.encode(mapPayload.data());

			if (payloadData == null) continue;
			extensionRepository.insert(entryEntity.getId(), mapPayload.id(), payloadData);
		}
	}

	private Map<Locale, String> toLocaleMap(Map<TranslationLocale, String> translations) {
		Map<Locale, String> resolved = new LinkedHashMap<>();
		for (Map.Entry<TranslationLocale, String> entry : translations.entrySet()) {
			Locale locale = toJavaLocale(entry.getKey());
			resolved.put(locale, entry.getValue());
		}

		return resolved;
	}

	private Locale toJavaLocale(TranslationLocale locale) {
		if (locale == null) return Locale.getDefault();
		if (locale instanceof SemanticLocale semanticLocale) return semanticLocale.unwrap();

		Locale.Builder builder = new Locale.Builder().setLanguage(locale.getLanguage());
		String country = locale.getCountry();
		if (country != null && !country.isBlank())
			builder.setRegion(country);
		String variant = locale.getVariant();
		if (variant != null && !variant.isBlank())
			builder.setVariant(variant);

		return builder.build();
	}

	private MessageType resolveEntryType(TranslationEntry entry) {
		if (entry instanceof LocalizedEntry)
			return MessageType.MESSAGE;

		return MessageType.TEMPLATE;
	}

	private String unescapeEntryKey(String entryKey) {
		if (entryKey == null || entryKey.isEmpty()) return entryKey;
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		for (int i = 0; i < entryKey.length(); i++) {
			char c = entryKey.charAt(i);
			if (escape) {
				builder.append(c);
				escape = false;
				continue;
			}

			if (c == '\\') {
				escape = true;
				continue;
			}

			builder.append(c);
		}

		if (escape) builder.append('\\');
		return builder.toString();
	}

	private Set<String> resolveNamespaces(Map<String, Path> filePaths) {
		Set<String> namespaces = new HashSet<>();
		for (String keyPrefix : filePaths.keySet()) {
			String namespace = NamespaceUtil.getNamespace(keyPrefix);
			if (namespace != null && !namespace.isBlank())
				namespaces.add(namespace);
		}

		if (namespaces.isEmpty()) {
			namespaces.add(Constants.Namespace.INTERNAL);
		}

		return namespaces;
	}

}
