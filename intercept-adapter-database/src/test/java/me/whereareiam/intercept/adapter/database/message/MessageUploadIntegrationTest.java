package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageUploadIntegrationTest extends BaseMessagePersistenceIntegrationTest {

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithTranslations(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageTranslationRepository translationRepo = translationRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		Map<Locale, String> translations = new HashMap<>();
		translations.put(Locale.US, "No permission");
		translations.put(Locale.GERMANY, "Keine Berechtigung");

		entries.put("errors.permissions.no-permission", localized(translations));
		filePaths.put("errors.permissions", resolveFile("errors/permissions.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("errors/permissions");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "no-permission");
		assertTrue(entryEntity.isPresent());

		List<MessageTranslationEntity> translationsList = translationRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(2, translationsList.size());

		Map<String, String> translationMap = new HashMap<>();
		for (MessageTranslationEntity t : translationsList) {
			Locale locale = t.getLocale();
			String localeStr = locale != null ? locale.toString() : "";
			translationMap.put(localeStr, t.getText());
		}

		assertEquals("No permission", translationMap.get("en_US"));
		assertEquals("Keine Berechtigung", translationMap.get("de_DE"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithSingleText(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageTranslationRepository translationRepo = translationRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("common.greeting", template("Hello <player>!"));
		filePaths.put("common", resolveFile("common/greeting.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("common/greeting");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "greeting");
		assertTrue(entryEntity.isPresent());
		assertEquals(MessageType.TEMPLATE, entryEntity.get().getEntryType());

		List<MessageTranslationEntity> translations = translationRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(1, translations.size());
		MessageTranslationEntity translation = translations.get(0);
		assertEquals("Hello <player>!", translation.getText());
		assertNull(translation.getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMultipleFiles(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("errors.file1.error1", template("Error 1"));
		filePaths.put("errors.file1", resolveFile("errors/file1.yml"));

		entries.put("errors.file2.error2", template("Error 2"));
		filePaths.put("errors.file2", resolveFile("errors/file2.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		assertTrue(fileRepo.findByFilePath("errors/file1").isPresent());
		assertTrue(fileRepo.findByFilePath("errors/file2").isPresent());
		assertEquals(2, fileRepo.count());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadEmptySnapshot(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageSnapshot snapshot = new MessageSnapshot(Map.of(), Map.of());
		assertDoesNotThrow(() -> service.uploadMessages(snapshot));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithRegexPatterns(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageRegexPatternRepository patternRepo = patternRepo(type);
		InterceptionRegistry interceptionRegistry = interceptionRegistry(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		Map<String, String> placeholders1 = new HashMap<>();
		placeholders1.put("player", "$1");
		placeholders1.put("message", "$2");
		CompiledRegexPattern pattern1 = new CompiledRegexPattern("^<(.+)> (.+)$", placeholders1, 10, false);

		Map<String, String> placeholders2 = new HashMap<>();
		placeholders2.put("permission", "$1");
		CompiledRegexPattern pattern2 = new CompiledRegexPattern("^You don't have permission: (.+)$", placeholders2, 5, true);

		List<CompiledRegexPattern> patterns = List.of(pattern1, pattern2);
		entries.put("chat.message", template("Chat message"));
		filePaths.put("chat", resolveFile("chat/message.yml"));
		interceptionRegistry.register("chat.message", patterns);

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("chat/message");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(2, patternsList.size());

		MessageRegexPatternEntity savedPattern1 = patternsList.stream()
				.filter(p -> p.getPattern().equals("^<(.+)> (.+)$"))
				.findFirst()
				.orElseThrow();
		assertEquals(10, savedPattern1.getPriority());
		assertFalse(savedPattern1.isReplaceMatched());
		assertEquals(0, savedPattern1.getSortOrder());

		MessageRegexPatternEntity savedPattern2 = patternsList.stream()
				.filter(p -> p.getPattern().equals("^You don't have permission: (.+)$"))
				.findFirst()
				.orElseThrow();
		assertEquals(5, savedPattern2.getPriority());
		assertTrue(savedPattern2.isReplaceMatched());
		assertEquals(1, savedPattern2.getSortOrder());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithRegexPatternsAndPlaceholders(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageRegexPatternRepository patternRepo = patternRepo(type);
		MessageRegexPlaceholderRepository placeholderRepo = placeholderRepo(type);
		InterceptionRegistry interceptionRegistry = interceptionRegistry(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("player", "$1");
		placeholders.put("message", "$2");
		placeholders.put("timestamp", "$3");
		CompiledRegexPattern pattern = new CompiledRegexPattern("^\\[(.+)\\] <(.+)> (.+)$", placeholders, 15, false);

		entries.put("chat.formatted", template("Formatted chat"));
		filePaths.put("chat", resolveFile("chat/formatted.yml"));
		interceptionRegistry.register("chat.formatted", List.of(pattern));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("chat/formatted");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "formatted");
		assertTrue(entryEntity.isPresent());

		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(1, patternsList.size());

		MessageRegexPatternEntity savedPattern = patternsList.get(0);
		assertEquals("^\\[(.+)\\] <(.+)> (.+)$", savedPattern.getPattern());
		assertEquals(15, savedPattern.getPriority());

		List<MessageRegexPlaceholderEntity> placeholdersList = placeholderRepo.findAllByPatternId(savedPattern.getId());
		assertEquals(3, placeholdersList.size());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithMultiplePatternsAndPlaceholders(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageRegexPatternRepository patternRepo = patternRepo(type);
		MessageRegexPlaceholderRepository placeholderRepo = placeholderRepo(type);
		InterceptionRegistry interceptionRegistry = interceptionRegistry(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		Map<String, String> placeholders1 = new HashMap<>();
		placeholders1.put("name", "$1");
		CompiledRegexPattern pattern1 = new CompiledRegexPattern("^Player: (.+)$", placeholders1, 20, false);

		Map<String, String> placeholders2 = new HashMap<>();
		placeholders2.put("sender", "$1");
		placeholders2.put("recipient", "$2");
		placeholders2.put("content", "$3");
		CompiledRegexPattern pattern2 = new CompiledRegexPattern("^(.+) -> (.+): (.+)$", placeholders2, 10, true);

		CompiledRegexPattern pattern3 = new CompiledRegexPattern("^System: .+$", null, 5, false);

		entries.put("messages.multi", template("Multi-pattern entry"));
		filePaths.put("messages", resolveFile("messages/multi.yml"));
		interceptionRegistry.register("messages.multi", List.of(pattern1, pattern2, pattern3));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("messages/multi");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "multi");
		assertTrue(entryEntity.isPresent());

		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(3, patternsList.size());

		patternsList.sort(Comparator.comparingInt(MessageRegexPatternEntity::getSortOrder));
		assertEquals(0, patternsList.get(0).getSortOrder());
		assertEquals(1, patternsList.get(1).getSortOrder());
		assertEquals(2, patternsList.get(2).getSortOrder());

		MessageRegexPatternEntity savedPattern1 = patternsList.get(0);
		List<MessageRegexPlaceholderEntity> placeholders1List = placeholderRepo.findAllByPatternId(savedPattern1.getId());
		assertEquals(1, placeholders1List.size());

		MessageRegexPatternEntity savedPattern2 = patternsList.get(1);
		List<MessageRegexPlaceholderEntity> placeholders2List = placeholderRepo.findAllByPatternId(savedPattern2.getId());
		assertEquals(3, placeholders2List.size());

		MessageRegexPatternEntity savedPattern3 = patternsList.get(2);
		List<MessageRegexPlaceholderEntity> placeholders3List = placeholderRepo.findAllByPatternId(savedPattern3.getId());
		assertEquals(0, placeholders3List.size());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithoutRegexPatterns(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageRegexPatternRepository patternRepo = patternRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("simple.message", template("Simple message"));
		filePaths.put("simple", resolveFile("simple/message.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePath("simple/message");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(0, patternsList.size());
	}

	private TranslationEntry template(String text) {
		return new TemplateEntry(text);
	}

	private TranslationEntry localized(Map<Locale, String> translations) {
		Map<TranslationLocale, String> mapped = new LinkedHashMap<>();
		for (Map.Entry<Locale, String> entry : translations.entrySet()) {
			mapped.put(SemanticLocale.wrap(entry.getKey()), entry.getValue());
		}
		return new LocalizedEntry(mapped);
	}
}
