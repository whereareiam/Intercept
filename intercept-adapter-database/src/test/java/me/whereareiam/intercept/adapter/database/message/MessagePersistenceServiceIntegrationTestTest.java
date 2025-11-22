package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageRegexPatternEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageRegexPlaceholderEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageTranslationEntity;
import me.whereareiam.intercept.adapter.database.repository.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageRegexPatternRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageRegexPlaceholderRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageTranslationRepository;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageSnapshot;
import me.whereareiam.intercept.type.PersistenceType;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MessagePersistenceServiceIntegrationTestTest extends BaseTest {
	private DefaultMessagePersistenceService postgresService;
	private DefaultMessagePersistenceService mariaDbService;
	private MessageFileRepository postgresFileRepo;
	private MessageFileRepository mariaDbFileRepo;
	private MessageEntryRepository postgresEntryRepo;
	private MessageEntryRepository mariaDbEntryRepo;
	private MessageTranslationRepository postgresTranslationRepo;
	private MessageTranslationRepository mariaDbTranslationRepo;
	private MessageRegexPatternRepository postgresPatternRepo;
	private MessageRegexPatternRepository mariaDbPatternRepo;
	private MessageRegexPlaceholderRepository postgresPlaceholderRepo;
	private MessageRegexPlaceholderRepository mariaDbPlaceholderRepo;

	@BeforeEach
	void setUp() {

		// Create repositories
		postgresFileRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbFileRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageFileRepository.class);
		postgresEntryRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageEntryRepository.class);
		mariaDbEntryRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageEntryRepository.class);
		postgresTranslationRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageTranslationRepository.class);
		mariaDbTranslationRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageTranslationRepository.class);
		postgresPatternRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageRegexPatternRepository.class);
		mariaDbPatternRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageRegexPatternRepository.class);
		postgresPlaceholderRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageRegexPlaceholderRepository.class);
		mariaDbPlaceholderRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageRegexPlaceholderRepository.class);

		// Create services
		postgresService = new DefaultMessagePersistenceService(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				postgresPatternRepo,
				postgresPlaceholderRepo,
				getJdbi(PersistenceType.POSTGRES)
		);
		mariaDbService = new DefaultMessagePersistenceService(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				mariaDbPatternRepo,
				mariaDbPlaceholderRepo,
				getJdbi(PersistenceType.MARIADB)
		);

		// Clear tables
		clearTables(PersistenceType.POSTGRES);
		clearTables(PersistenceType.MARIADB);
	}

	private void clearTables(PersistenceType type) {
		getJdbi(type).useHandle(handle -> {
			handle.execute("DELETE FROM intercept_message_regex_placeholders");
			handle.execute("DELETE FROM intercept_message_regex_patterns");
			handle.execute("DELETE FROM intercept_message_translations");
			handle.execute("DELETE FROM intercept_message_entries");
			handle.execute("DELETE FROM intercept_message_files");
		});
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithTranslations(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageTranslationRepository translationRepo = type == PersistenceType.POSTGRES ? postgresTranslationRepo : mariaDbTranslationRepo;

		// Create snapshot with translations
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		Map<Locale, String> translations = new HashMap<>();
		translations.put(Locale.US, "No permission");
		translations.put(Locale.GERMANY, "Keine Berechtigung");

		TestMessageEntry entry = new TestMessageEntry(MessageType.MESSAGE, translations);
		entries.put("errors.permissions.no-permission", entry);
		filePaths.put("errors.permissions", Paths.get("errors/permissions.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify file was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("errors/permissions.yml");
		assertTrue(file.isPresent());
		assertEquals("errors/permissions.yml", file.get().getFilePath());

		// Verify entry was created
		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "no-permission");
		assertTrue(entryEntity.isPresent());
		assertEquals("no-permission", entryEntity.get().getEntryKey());

		// Verify translations were created
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
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithSingleText(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageTranslationRepository translationRepo = type == PersistenceType.POSTGRES ? postgresTranslationRepo : mariaDbTranslationRepo;

		// Create snapshot with single text
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		TestMessageEntry entry = new TestMessageEntry(MessageType.TEMPLATE, "Hello {player}!");
		entries.put("common.greeting", entry);
		filePaths.put("common", Paths.get("common/greeting.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify entry was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("common/greeting.yml");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "greeting");
		assertTrue(entryEntity.isPresent());
		assertEquals(MessageType.TEMPLATE, entryEntity.get().getEntryType());

		// Verify single translation was created with null locale (maps to empty string in DB)
		List<MessageTranslationEntity> translations = translationRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(1, translations.size());
		MessageTranslationEntity translation = translations.get(0);
		assertEquals("Hello {player}!", translation.getText());
		// Locale should be null for single-language entries (empty string in database)
		assertNull(translation.getLocale());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMultipleFiles(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;

		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		// File 1
		TestMessageEntry entry1 = new TestMessageEntry(MessageType.MESSAGE, "Error 1");
		entries.put("errors.file1.error1", entry1);
		filePaths.put("errors.file1", Paths.get("errors/file1.yml"));

		// File 2
		TestMessageEntry entry2 = new TestMessageEntry(MessageType.MESSAGE, "Error 2");
		entries.put("errors.file2.error2", entry2);
		filePaths.put("errors.file2", Paths.get("errors/file2.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify both files were created
		assertTrue(fileRepo.findByFilePath("errors/file1.yml").isPresent());
		assertTrue(fileRepo.findByFilePath("errors/file2.yml").isPresent());
		assertEquals(2, fileRepo.count());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadEmptySnapshot(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;

		MessageSnapshot snapshot = new MessageSnapshot(Map.of(), Map.of());

		// Should not throw
		assertDoesNotThrow(() -> service.uploadMessages(snapshot));
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithRegexPatterns(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageRegexPatternRepository patternRepo = type == PersistenceType.POSTGRES ? postgresPatternRepo : mariaDbPatternRepo;

		// Create snapshot with regex patterns
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		// Create regex patterns
		Map<String, String> placeholders1 = new HashMap<>();
		placeholders1.put("player", "$1");
		placeholders1.put("message", "$2");
		CompiledRegexPattern pattern1 = new CompiledRegexPattern(
				"^<(.+)> (.+)$",
				placeholders1,
				10,
				false
		);

		Map<String, String> placeholders2 = new HashMap<>();
		placeholders2.put("permission", "$1");
		CompiledRegexPattern pattern2 = new CompiledRegexPattern(
				"^You don't have permission: (.+)$",
				placeholders2,
				5,
				true
		);

		List<CompiledRegexPattern> patterns = List.of(pattern1, pattern2);
		TestMessageEntry entry = new TestMessageEntry(MessageType.MESSAGE, "Chat message", patterns);
		entries.put("chat.message", entry);
		filePaths.put("chat", Paths.get("chat/message.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify entry was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("chat/message.yml");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		// Verify patterns were created
		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(2, patternsList.size());

		// Verify first pattern (higher priority, sort_order 0)
		MessageRegexPatternEntity savedPattern1 = patternsList.stream()
				.filter(p -> p.getPattern().equals("^<(.+)> (.+)$"))
				.findFirst()
				.orElseThrow();
		assertEquals("^<(.+)> (.+)$", savedPattern1.getPattern());
		assertEquals(10, savedPattern1.getPriority());
		assertFalse(savedPattern1.isReplaceMatched());
		assertEquals(0, savedPattern1.getSortOrder());

		// Verify second pattern (lower priority, sort_order 1)
		MessageRegexPatternEntity savedPattern2 = patternsList.stream()
				.filter(p -> p.getPattern().equals("^You don't have permission: (.+)$"))
				.findFirst()
				.orElseThrow();
		assertEquals("^You don't have permission: (.+)$", savedPattern2.getPattern());
		assertEquals(5, savedPattern2.getPriority());
		assertTrue(savedPattern2.isReplaceMatched());
		assertEquals(1, savedPattern2.getSortOrder());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithRegexPatternsAndPlaceholders(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageRegexPatternRepository patternRepo = type == PersistenceType.POSTGRES ? postgresPatternRepo : mariaDbPatternRepo;
		MessageRegexPlaceholderRepository placeholderRepo = type == PersistenceType.POSTGRES ? postgresPlaceholderRepo : mariaDbPlaceholderRepo;

		// Create snapshot with regex patterns and placeholders
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		// Create regex pattern with placeholders
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("player", "$1");
		placeholders.put("message", "$2");
		placeholders.put("timestamp", "$3");
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"^\\[(.+)\\] <(.+)> (.+)$",
				placeholders,
				15,
				false
		);

		List<CompiledRegexPattern> patterns = List.of(pattern);
		TestMessageEntry entry = new TestMessageEntry(MessageType.MESSAGE, "Formatted chat", patterns);
		entries.put("chat.formatted", entry);
		filePaths.put("chat", Paths.get("chat/formatted.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify entry was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("chat/formatted.yml");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "formatted");
		assertTrue(entryEntity.isPresent());

		// Verify pattern was created
		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(1, patternsList.size());

		MessageRegexPatternEntity savedPattern = patternsList.get(0);
		assertEquals("^\\[(.+)\\] <(.+)> (.+)$", savedPattern.getPattern());
		assertEquals(15, savedPattern.getPriority());
		assertFalse(savedPattern.isReplaceMatched());

		// Verify placeholders were created
		List<MessageRegexPlaceholderEntity> placeholdersList = placeholderRepo.findAllByPatternId(savedPattern.getId());
		assertEquals(3, placeholdersList.size());

		Map<String, String> placeholderMap = new HashMap<>();
		for (MessageRegexPlaceholderEntity p : placeholdersList) {
			placeholderMap.put(p.getPlaceholderName(), p.getCaptureGroup());
		}

		assertEquals("$1", placeholderMap.get("player"));
		assertEquals("$2", placeholderMap.get("message"));
		assertEquals("$3", placeholderMap.get("timestamp"));
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithMultiplePatternsAndPlaceholders(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageRegexPatternRepository patternRepo = type == PersistenceType.POSTGRES ? postgresPatternRepo : mariaDbPatternRepo;
		MessageRegexPlaceholderRepository placeholderRepo = type == PersistenceType.POSTGRES ? postgresPlaceholderRepo : mariaDbPlaceholderRepo;

		// Create snapshot with multiple patterns
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		// Pattern 1: Simple pattern with one placeholder
		Map<String, String> placeholders1 = new HashMap<>();
		placeholders1.put("name", "$1");
		CompiledRegexPattern pattern1 = new CompiledRegexPattern(
				"^Player: (.+)$",
				placeholders1,
				20,
				false
		);

		// Pattern 2: Complex pattern with multiple placeholders
		Map<String, String> placeholders2 = new HashMap<>();
		placeholders2.put("sender", "$1");
		placeholders2.put("recipient", "$2");
		placeholders2.put("content", "$3");
		CompiledRegexPattern pattern2 = new CompiledRegexPattern(
				"^(.+) -> (.+): (.+)$",
				placeholders2,
				10,
				true
		);

		// Pattern 3: Pattern without placeholders
		CompiledRegexPattern pattern3 = new CompiledRegexPattern(
				"^System: .+$",
				null,
				5,
				false
		);

		List<CompiledRegexPattern> patterns = List.of(pattern1, pattern2, pattern3);
		TestMessageEntry entry = new TestMessageEntry(MessageType.MESSAGE, "Multi-pattern entry", patterns);
		entries.put("messages.multi", entry);
		filePaths.put("messages", Paths.get("messages/multi.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify entry was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("messages/multi.yml");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "multi");
		assertTrue(entryEntity.isPresent());

		// Verify all patterns were created with correct sort order
		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(3, patternsList.size());

		// Verify sort order
		patternsList.sort(Comparator.comparingInt(MessageRegexPatternEntity::getSortOrder));
		assertEquals(0, patternsList.get(0).getSortOrder());
		assertEquals(1, patternsList.get(1).getSortOrder());
		assertEquals(2, patternsList.get(2).getSortOrder());

		// Verify placeholders for pattern 1
		MessageRegexPatternEntity savedPattern1 = patternsList.get(0);
		List<MessageRegexPlaceholderEntity> placeholders1List = placeholderRepo.findAllByPatternId(savedPattern1.getId());
		assertEquals(1, placeholders1List.size());
		assertEquals("name", placeholders1List.get(0).getPlaceholderName());
		assertEquals("$1", placeholders1List.get(0).getCaptureGroup());

		// Verify placeholders for pattern 2
		MessageRegexPatternEntity savedPattern2 = patternsList.get(1);
		List<MessageRegexPlaceholderEntity> placeholders2List = placeholderRepo.findAllByPatternId(savedPattern2.getId());
		assertEquals(3, placeholders2List.size());

		// Verify placeholders for pattern 3 (should have none)
		MessageRegexPatternEntity savedPattern3 = patternsList.get(2);
		List<MessageRegexPlaceholderEntity> placeholders3List = placeholderRepo.findAllByPatternId(savedPattern3.getId());
		assertEquals(0, placeholders3List.size());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUploadMessagesWithoutRegexPatterns(PersistenceType type) {
		DefaultMessagePersistenceService service = type == PersistenceType.POSTGRES ? postgresService : mariaDbService;
		MessageFileRepository fileRepo = type == PersistenceType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
		MessageEntryRepository entryRepo = type == PersistenceType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
		MessageRegexPatternRepository patternRepo = type == PersistenceType.POSTGRES ? postgresPatternRepo : mariaDbPatternRepo;

		// Create snapshot without regex patterns
		Map<String, MessageEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		TestMessageEntry entry = new TestMessageEntry(MessageType.MESSAGE, "Simple message");
		entries.put("simple.message", entry);
		filePaths.put("simple", Paths.get("simple/message.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		// Upload
		service.uploadMessages(snapshot);

		// Verify entry was created
		Optional<MessageFileEntity> file = fileRepo.findByFilePath("simple/message.yml");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		// Verify no patterns were created
		List<MessageRegexPatternEntity> patternsList = patternRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(0, patternsList.size());
	}
}

