package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.message.MessageKeyResolver;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
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

@Singleton
public class MessageUploadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageRegexPatternRepository patternRepository;
	private final MessageRegexPlaceholderRepository placeholderRepository;
	private final InterceptionRegistry interceptionRegistry;
	private final Path messagesPath;

	@Inject
	public MessageUploadCoordinator(
			MessageFileRepository fileRepository,
			MessageEntryRepository entryRepository,
			MessageTranslationRepository translationRepository,
			MessageRegexPatternRepository patternRepository,
			MessageRegexPlaceholderRepository placeholderRepository,
			InterceptionRegistry interceptionRegistry,
			@Named("messagesPath") Path messagesPath
	) {
		this.fileRepository = fileRepository;
		this.entryRepository = entryRepository;
		this.translationRepository = translationRepository;
		this.patternRepository = patternRepository;
		this.placeholderRepository = placeholderRepository;
		this.interceptionRegistry = interceptionRegistry;
		this.messagesPath = messagesPath;
	}

	public void upload(MessageSnapshot snapshot) {
		placeholderRepository.truncateAll();
		patternRepository.truncateAll();
		translationRepository.truncateAll();
		entryRepository.truncateAll();
		fileRepository.truncateAll();

		Map<String, MessageFileEntity> fileEntities = createFileEntities(snapshot.getFilePaths());
		processEntries(snapshot.getEntries(), snapshot.getFilePaths(), fileEntities);
	}

	private Map<String, MessageFileEntity> createFileEntities(Map<String, Path> filePaths) {
		Map<String, MessageFileEntity> fileEntities = new HashMap<>();

		for (Map.Entry<String, Path> fileEntry : filePaths.entrySet()) {
			String keyPrefix = fileEntry.getKey();
			Path filePath = fileEntry.getValue();

			Path relativePath = messagesPath.relativize(filePath);
			String relativePathString = relativePath.toString().replace('\\', '/');

			int lastDot = relativePathString.lastIndexOf('.');
			if (lastDot > 0) relativePathString = relativePathString.substring(0, lastDot);

			MessageFileEntity newFile = new MessageFileEntity();
			newFile.setFilePath(relativePathString);
			MessageFileEntity savedFile = fileRepository.save(newFile);

			fileEntities.put(keyPrefix, savedFile);
		}

		return fileEntities;
	}

	private void processEntries(
			Map<String, TranslationEntry> entries,
			Map<String, Path> filePaths,
			Map<String, MessageFileEntity> fileEntities
	) {
		for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
			String fullKey = entry.getKey();
			TranslationEntry messageEntry = entry.getValue();

			String keyPrefix = MessageKeyResolver.findKeyPrefix(fullKey, filePaths.keySet());
			MessageFileEntity fileEntity = fileEntities.get(keyPrefix);

			if (fileEntity == null) continue;

			MessageEntryEntity entryEntity = entryRepository.save(MessageEntryEntity.builder()
					.file(fileEntity)
					.entryKey(MessageKeyResolver.extractEntryKey(fullKey, keyPrefix))
					.entryType(resolveEntryType(messageEntry))
					.build());

			processTranslations(entryEntity, messageEntry);
			processRegexPatterns(entryEntity, fullKey);
		}
	}

	private void processTranslations(MessageEntryEntity entryEntity, TranslationEntry messageEntry) {
		if (messageEntry instanceof LocalizedEntry localizedEntry) {
			Map<TranslationLocale, String> translations = localizedEntry.getTranslations();
			Map<Locale, String> resolved = toLocaleMap(translations);
			for (Map.Entry<Locale, String> translation : resolved.entrySet()) {
				translationRepository.insert(entryEntity.getId(), translation.getKey(), translation.getValue());
			}
			return;
		}

		if (messageEntry instanceof TemplateEntry templateEntry) {
			String text = templateEntry.getTemplate();
			if (text == null) return;
			translationRepository.insert(entryEntity.getId(), null, text);
		}
	}

	private void processRegexPatterns(MessageEntryEntity entryEntity, String fullKey) {
		if (interceptionRegistry == null) return;

		int sortOrder = 0;
		for (CompiledRegexPattern compiledPattern : interceptionRegistry.get(fullKey)) {
			long patternId = patternRepository.insert(
					entryEntity.getId(),
					compiledPattern.getRegex(),
					compiledPattern.getPriority(),
					compiledPattern.isReplaceMatched(),
					sortOrder++
			);

			processPlaceholders(patternId, compiledPattern);
		}
	}

	private void processPlaceholders(long patternId, CompiledRegexPattern compiledPattern) {
		Map<String, String> placeholders = compiledPattern.getPlaceholders();
		if (placeholders == null || placeholders.isEmpty()) return;

		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			String placeholderName = entry.getKey();
			String captureGroup = entry.getValue();
			if (placeholderName == null || captureGroup == null) continue;

			placeholderRepository.insert(patternId, placeholderName, captureGroup);
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

	private me.whereareiam.intercept.type.message.MessageType resolveEntryType(TranslationEntry entry) {
		if (entry instanceof LocalizedEntry) return me.whereareiam.intercept.type.message.MessageType.MESSAGE;
		return me.whereareiam.intercept.type.message.MessageType.TEMPLATE;
	}
}
