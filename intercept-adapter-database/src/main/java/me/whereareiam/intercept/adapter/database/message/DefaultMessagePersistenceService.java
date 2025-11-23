package me.whereareiam.intercept.adapter.database.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageSnapshot;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import org.jdbi.v3.core.Jdbi;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of MessagePersistenceService.
 * Orchestrates the upload and download of messages to/from the database.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultMessagePersistenceService implements MessagePersistenceService {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageRegexPatternRepository patternRepository;
	private final MessageRegexPlaceholderRepository placeholderRepository;
	private final Jdbi jdbi;

	@Override
	public void uploadMessages(MessageSnapshot snapshot) {
		if (snapshot == null) throw new IllegalArgumentException("Snapshot cannot be null");

		Map<String, MessageEntry> entries = snapshot.getEntries();
		Map<String, Path> filePaths = snapshot.getFilePaths();

		if (entries == null || entries.isEmpty() || filePaths == null) {
			Logger.debug("No entries to upload");
			return;
		}

		jdbi.useTransaction(handle -> {
			placeholderRepository.truncateAll();
			patternRepository.truncateAll();
			translationRepository.truncateAll();
			entryRepository.truncateAll();
			fileRepository.truncateAll();

			Map<String, MessageFileEntity> fileEntities = createFileEntities(filePaths);
			processEntries(entries, filePaths, fileEntities);
			Logger.info("Uploaded %d entries from %d files", entries.size(), filePaths.size());
		});
	}

	@Override
	public MessageSnapshot downloadMessages() {
		// TODO: Implement download
		throw new UnsupportedOperationException("Download not yet implemented");
	}

	/**
	 * Processes all message entries and creates corresponding database entities.
	 *
	 * @param entries      map of full key to message entry
	 * @param filePaths    map of key prefix to file path
	 * @param fileEntities map of key prefix to file entity
	 */
	private void processEntries(Map<String, MessageEntry> entries, Map<String, Path> filePaths, Map<String, MessageFileEntity> fileEntities) {
		for (Map.Entry<String, MessageEntry> entry : entries.entrySet()) {
			String fullKey = entry.getKey();
			MessageEntry messageEntry = entry.getValue();

			String keyPrefix = MessageKeyResolver.findKeyPrefix(fullKey, filePaths.keySet());
			MessageFileEntity fileEntity = fileEntities.get(keyPrefix);

			if (fileEntity == null) {
				Logger.warn("No file entity found for key: %s", fullKey);
				continue;
			}

			MessageEntryEntity entryEntity = entryRepository.save(MessageEntryEntity.builder()
					.file(fileEntity)
					.entryKey(MessageKeyResolver.extractEntryKey(fullKey, keyPrefix))
					.entryType(messageEntry.getType())
					.build());

			processTranslations(entryEntity, messageEntry);
			processRegexPatterns(entryEntity, messageEntry);
		}
	}

	/**
	 * Creates file entities for all file paths.
	 * Since all files are deleted before this method is called, it always creates new entities.
	 *
	 * @param filePaths map of key prefix to file path
	 * @return map of key prefix to file entity
	 */
	private Map<String, MessageFileEntity> createFileEntities(Map<String, Path> filePaths) {
		Map<String, MessageFileEntity> fileEntities = new HashMap<>();

		for (Map.Entry<String, Path> fileEntry : filePaths.entrySet()) {
			String keyPrefix = fileEntry.getKey();
			Path filePath = fileEntry.getValue();
			String relativePath = filePath.toString().replace('\\', '/');

			MessageFileEntity newFile = new MessageFileEntity();
			newFile.setFilePath(relativePath);
			MessageFileEntity savedFile = fileRepository.save(newFile);

			fileEntities.put(keyPrefix, savedFile);
		}

		return fileEntities;
	}

	/**
	 * Creates translations for an entry.
	 * For single-language entries, uses null Locale (which maps to empty string in database).
	 * Note: All translations are already deleted at the start of upload, so we only need to create new ones.
	 *
	 * @param entryEntity  the entry entity
	 * @param messageEntry the message entry with translations or single text
	 */
	private void processTranslations(MessageEntryEntity entryEntity, MessageEntry messageEntry) {
		if (messageEntry.hasTranslations()) {
			// Multi-language: create translations for each locale
			for (Locale locale : messageEntry.getLocales()) {
				String text = messageEntry.getText(locale);
				if (text == null) continue;
				translationRepository.insert(entryEntity.getId(), locale, text);
			}

			return;
		}

		// Single-language: use null Locale (which maps to empty string in database)
		String text = messageEntry.getText();
		if (text == null) return;
		translationRepository.insert(entryEntity.getId(), null, text);
	}

	/**
	 * Creates regex patterns and placeholders for an entry.
	 * Note: All patterns and placeholders are already deleted at the start of upload, so we only need to create new ones.
	 *
	 * @param entryEntity  the entry entity
	 * @param messageEntry the message entry with regex patterns
	 */
	private void processRegexPatterns(MessageEntryEntity entryEntity, MessageEntry messageEntry) {
		if (!messageEntry.hasRegexPatterns()) return;

		List<CompiledRegexPattern> patterns = messageEntry.getRegexPatterns();
		if (patterns == null || patterns.isEmpty()) return;

		int sortOrder = 0;
		for (CompiledRegexPattern compiledPattern : patterns) {
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

	/**
	 * Creates placeholders for a regex pattern.
	 *
	 * @param patternId       the pattern entity ID
	 * @param compiledPattern the compiled regex pattern with placeholders
	 */
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

