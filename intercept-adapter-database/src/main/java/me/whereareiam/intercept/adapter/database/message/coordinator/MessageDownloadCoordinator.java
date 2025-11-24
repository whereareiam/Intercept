package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.LocaleUtil;

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

		Map<String, CompiledMessageEntry> entrySnapshot = new LinkedHashMap<>();
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
		Map<String, MessageDocumentEntry> fileItems = new LinkedHashMap<>();
		Map<String, CompiledMessageEntry> snapshotEntries = new LinkedHashMap<>();
		List<MessageType> entryTypes = new ArrayList<>();

		for (MessageEntryEntity entryEntity : entryEntities) {
			EntryAssembly entryAssembly = assembleEntry(fileEntity.getKeyPrefix(), entryEntity);
			fileItems.put(entryEntity.getEntryKey(), entryAssembly.fileData());
			snapshotEntries.put(entryAssembly.fullKey(), entryAssembly.snapshotEntry());

			if (entryAssembly.entryType() != null)
				entryTypes.add(entryAssembly.entryType());
		}

		MessageDocument fileData = new MessageDocument();
		fileData.setItems(fileItems);
		fileData.setType(resolveFileType(entryTypes));

		return new AssemblyResult(fileData, snapshotEntries);
	}

	private EntryAssembly assembleEntry(String keyPrefix, MessageEntryEntity entryEntity) {
		String entryKey = entryEntity.getEntryKey();
		String fullKey = buildFullKey(keyPrefix, entryKey);
		MessageType entryType = entryEntity.getEntryType();

		TranslationAssembly translations = loadTranslations(entryEntity.getId());

		MessageDocumentEntry fileData = new MessageDocumentEntry();
		fileData.setType(entryType);
		fileData.setText(translations.defaultText());
		fileData.setTranslations(toDocumentTranslations(translations.translations()));

		RegexAssemblyResult regexAssembly = assembleRegex(entryEntity.getId());
		fileData.setRegex(regexAssembly.fileRegex().isEmpty() ? null : regexAssembly.fileRegex());

		CompiledMessageEntry snapshotEntry = buildSnapshotEntry(
				entryType,
				translations,
				regexAssembly.snapshotRegex()
		);

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

	private Map<String, Object> toDocumentTranslations(Map<Locale, String> translations) {
		if (translations == null || translations.isEmpty()) return null;

		Map<String, Object> documentTranslations = new LinkedHashMap<>();
		for (Map.Entry<Locale, String> entry : translations.entrySet()) {
			Locale locale = entry.getKey();
			String localeKey = locale != null ? LocaleUtil.formatLocale(locale) : "default";
			documentTranslations.put(localeKey, entry.getValue());
		}

		return documentTranslations;
	}

	private RegexAssemblyResult assembleRegex(long entryId) {
		List<MessageRegexPatternEntity> patternEntities = patternRepository.findAllByEntryId(entryId);
		if (patternEntities.isEmpty()) return new RegexAssemblyResult(List.of(), List.of());

		List<CompiledRegexPattern> snapshotPatterns = new ArrayList<>();
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

			CompiledRegexPattern snapshotPattern = new CompiledRegexPattern(
					patternEntity.getPattern(),
					placeholders,
					patternEntity.getPriority(),
					patternEntity.isReplaceMatched()
			);

			MessageDocumentRegex fileRegexEntry = new MessageDocumentRegex();
			fileRegexEntry.setPattern(patternEntity.getPattern());
			fileRegexEntry.setPriority(patternEntity.getPriority());
			fileRegexEntry.setReplaceMatched(patternEntity.isReplaceMatched());
			fileRegexEntry.setPlaceholders(placeholders);

			snapshotPatterns.add(snapshotPattern);
			fileRegex.add(fileRegexEntry);
		}

		return new RegexAssemblyResult(fileRegex, snapshotPatterns);
	}

	private MessageType resolveFileType(List<MessageType> entryTypes) {
		if (entryTypes.isEmpty()) return null;
		boolean allSame = entryTypes.stream().distinct().count() == 1;

		return allSame ? entryTypes.get(0) : null;
	}

	private String buildFullKey(String keyPrefix, String entryKey) {
		return keyPrefix == null || keyPrefix.isBlank() ? entryKey : keyPrefix + "." + entryKey;
	}

	private CompiledMessageEntry buildSnapshotEntry(
			MessageType entryType, TranslationAssembly translations, List<CompiledRegexPattern> regexPatterns
	) {
		if (translations.translations() != null && !translations.translations().isEmpty())
			return new CompiledMessageEntry(entryType, translations.translations(), regexPatterns);

		return new CompiledMessageEntry(entryType, translations.defaultText(), regexPatterns);
	}

	private record AssemblyResult(MessageDocument fileData, Map<String, CompiledMessageEntry> snapshotEntries) {}

	private record EntryAssembly(String fullKey, CompiledMessageEntry snapshotEntry, MessageDocumentEntry fileData,
	                             MessageType entryType) {}

	private record RegexAssemblyResult(List<MessageDocumentRegex> fileRegex,
	                                   List<CompiledRegexPattern> snapshotRegex) {}

	private record TranslationAssembly(String defaultText, Map<Locale, String> translations) {}
}