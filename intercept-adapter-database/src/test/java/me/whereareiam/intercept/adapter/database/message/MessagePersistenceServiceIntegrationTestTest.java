package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageTranslationEntity;
import me.whereareiam.intercept.adapter.database.repository.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageTranslationRepository;
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

	@BeforeEach
	void setUp() {

		// Create repositories
		postgresFileRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbFileRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageFileRepository.class);
		postgresEntryRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageEntryRepository.class);
		mariaDbEntryRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageEntryRepository.class);
		postgresTranslationRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageTranslationRepository.class);
		mariaDbTranslationRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageTranslationRepository.class);

		// Create services
		postgresService = new DefaultMessagePersistenceService(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				getJdbi(PersistenceType.POSTGRES)
		);
		mariaDbService = new DefaultMessagePersistenceService(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				getJdbi(PersistenceType.MARIADB)
		);

		// Clear tables
		clearTables(PersistenceType.POSTGRES);
		clearTables(PersistenceType.MARIADB);
	}

	private void clearTables(PersistenceType type) {
		getJdbi(type).useHandle(handle -> {
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
}

