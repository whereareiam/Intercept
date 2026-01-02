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
import me.whereareiam.intercept.model.messaging.document.MessageDocumentInterception;
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
		Map<String, Object> fileItems = new LinkedHashMap<>();
		Map<String, CompiledMessageEntry> snapshotEntries = new LinkedHashMap<>();

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
		Map<String, Object> locales = toDocumentLocales(translations);
		if (locales != null && !locales.isEmpty()) {
			fileData.setLocales(locales);
		} else {
			fileData.setText(translations.defaultText());
		}

		RegexAssemblyResult regexAssembly = assembleRegex(entryEntity.getId());
		fileData.setInterception(regexAssembly.fileInterception());

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

	private Map<String, Object> toDocumentLocales(TranslationAssembly translations) {
		if ((translations.defaultText() == null || translations.defaultText().isEmpty())
				&& (translations.translations() == null || translations.translations().isEmpty())) {
			return null;
		}

		Map<String, Object> documentLocales = new LinkedHashMap<>();
		if (translations.defaultText() != null) {
			documentLocales.put("default", translations.defaultText());
		}

		if (translations.translations() != null) {
			for (Map.Entry<Locale, String> entry : translations.translations().entrySet()) {
				Locale locale = entry.getKey();
				String localeKey = locale != null ? LocaleUtil.formatLocale(locale) : "default";
				documentLocales.put(localeKey, entry.getValue());
			}
		}

		return documentLocales;
	}

	private RegexAssemblyResult assembleRegex(long entryId) {
		List<MessageRegexPatternEntity> patternEntities = patternRepository.findAllByEntryId(entryId);
		if (patternEntities.isEmpty()) return new RegexAssemblyResult(null, List.of());

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

		MessageDocumentInterception interception = new MessageDocumentInterception();
		interception.setPatterns(fileRegex);
		return new RegexAssemblyResult(interception, snapshotPatterns);
	}

	private String buildFullKey(String keyPrefix, String entryKey) {
		return keyPrefix == null || keyPrefix.isBlank() ? entryKey : keyPrefix + "." + entryKey;
	}

	private CompiledMessageEntry buildSnapshotEntry(
			MessageType entryType, TranslationAssembly translations, List<CompiledRegexPattern> regexPatterns
	) {
		MessageType resolvedType = entryType;
		if (resolvedType == null) {
			resolvedType = translations.translations() != null && !translations.translations().isEmpty()
					? MessageType.MESSAGE
					: MessageType.TEMPLATE;
		}

		if (translations.translations() != null && !translations.translations().isEmpty())
			return new CompiledMessageEntry(resolvedType, translations.translations(), regexPatterns);

		return new CompiledMessageEntry(resolvedType, translations.defaultText(), regexPatterns);
	}

	private record AssemblyResult(MessageDocument fileData, Map<String, CompiledMessageEntry> snapshotEntries) {}

	private record EntryAssembly(String fullKey, CompiledMessageEntry snapshotEntry, MessageDocumentEntry fileData,
	                             MessageType entryType) {}

	private record RegexAssemblyResult(MessageDocumentInterception fileInterception,
	                                   List<CompiledRegexPattern> snapshotRegex) {}

	private record TranslationAssembly(String defaultText, Map<Locale, String> translations) {}
}
