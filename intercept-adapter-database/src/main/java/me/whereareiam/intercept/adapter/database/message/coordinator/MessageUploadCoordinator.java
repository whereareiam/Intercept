package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.message.MessageKeyResolver;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Singleton
public class MessageUploadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageRegexPatternRepository patternRepository;
	private final MessageRegexPlaceholderRepository placeholderRepository;
	private final Path messagesPath;

	@Inject
	public MessageUploadCoordinator(
			MessageFileRepository fileRepository,
			MessageEntryRepository entryRepository,
			MessageTranslationRepository translationRepository,
			MessageRegexPatternRepository patternRepository,
			MessageRegexPlaceholderRepository placeholderRepository,
			@Named("messagesPath") Path messagesPath
	) {
		this.fileRepository = fileRepository;
		this.entryRepository = entryRepository;
		this.translationRepository = translationRepository;
		this.patternRepository = patternRepository;
		this.placeholderRepository = placeholderRepository;
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
			Map<String, CompiledMessageEntry> entries,
			Map<String, Path> filePaths,
			Map<String, MessageFileEntity> fileEntities
	) {
		for (Map.Entry<String, CompiledMessageEntry> entry : entries.entrySet()) {
			String fullKey = entry.getKey();
			CompiledMessageEntry messageEntry = entry.getValue();

			String keyPrefix = MessageKeyResolver.findKeyPrefix(fullKey, filePaths.keySet());
			MessageFileEntity fileEntity = fileEntities.get(keyPrefix);

			if (fileEntity == null) continue;

			MessageEntryEntity entryEntity = entryRepository.save(MessageEntryEntity.builder()
					.file(fileEntity)
					.entryKey(MessageKeyResolver.extractEntryKey(fullKey, keyPrefix))
					.entryType(messageEntry.getType())
					.build());

			processTranslations(entryEntity, messageEntry);
			processRegexPatterns(entryEntity, messageEntry);
		}
	}

	private void processTranslations(MessageEntryEntity entryEntity, CompiledMessageEntry messageEntry) {
		if (messageEntry.hasTranslations()) {
			for (Locale locale : messageEntry.getLocales()) {
				String text = messageEntry.getText(locale);
				if (text == null) continue;
				translationRepository.insert(entryEntity.getId(), locale, text);
			}

			return;
		}

		String text = messageEntry.getText();
		if (text == null) return;

		translationRepository.insert(entryEntity.getId(), null, text);
	}

	private void processRegexPatterns(MessageEntryEntity entryEntity, CompiledMessageEntry messageEntry) {
		if (!messageEntry.hasRegexPatterns()) return;

		int sortOrder = 0;
		for (CompiledRegexPattern compiledPattern : messageEntry.getRegexPatterns()) {
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
}