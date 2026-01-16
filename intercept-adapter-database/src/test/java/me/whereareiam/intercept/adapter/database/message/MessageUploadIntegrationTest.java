package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.NamespaceUtil;
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
		translations.put(Locale.ENGLISH, "No permission");
		translations.put(Locale.GERMANY, "Keine Berechtigung");

		entries.put("errors.permissions.no-permission", localized(translations));
		filePaths.put("errors.permissions", resolveFile("errors/permissions.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePathAndNamespace(
				Constants.Namespace.INTERNAL, "errors/permissions");
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

		assertEquals("No permission", translationMap.get("en"));
		assertEquals("Keine Berechtigung", translationMap.get("de_DE"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithSingleText(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageTranslationRepository translationRepo = translationRepo(type);
		MessageTemplateRepository templateRepo = templateRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("common.greeting", template("Hello <player>!"));
		filePaths.put("common", resolveFile("common/greeting.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePathAndNamespace(
				Constants.Namespace.INTERNAL, "common/greeting");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "greeting");
		assertTrue(entryEntity.isPresent());
		assertEquals(MessageType.TEMPLATE, entryEntity.get().getEntryType());

		List<MessageTranslationEntity> translations = translationRepo.findAllByEntryId(entryEntity.get().getId());
		assertTrue(translations.isEmpty());

		MessageTemplateEntity template = templateRepo.findByEntryId(entryEntity.get().getId());
		assertNotNull(template);
		assertEquals("Hello <player>!", template.getText());
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

		assertTrue(fileRepo.findByFilePathAndNamespace(Constants.Namespace.INTERNAL, "errors/file1").isPresent());
		assertTrue(fileRepo.findByFilePathAndNamespace(Constants.Namespace.INTERNAL, "errors/file2").isPresent());
		assertEquals(2, fileRepo.count());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testReuploadDoesNotDuplicateFiles(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("oraylen:en.greeting", template("Hello"));
		filePaths.put("oraylen:en", resolveFile("en.yml"));

		MessageSnapshot snapshot = new MessageSnapshot(entries, filePaths);

		service.uploadMessages(snapshot);
		assertDoesNotThrow(() -> service.uploadMessages(snapshot));

		Optional<MessageFileEntity> file = fileRepo.findByFilePathAndNamespace("oraylen", "en");
		assertTrue(file.isPresent());
		assertEquals(1, fileRepo.count());

		List<MessageEntryEntity> storedEntries = entryRepo.findAllByFileId(file.get().getId());
		assertEquals(1, storedEntries.size());
		assertEquals("greeting", storedEntries.get(0).getEntryKey());
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
	void testUploadMessagesWithExtensions(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageExtensionRepository extensionRepo = extensionRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();
		Map<String, MessageExtensions> extensionData = new HashMap<>();

		entries.put("chat.message", template("Chat message"));
		filePaths.put("chat", resolveFile("chat/message.yml"));

		Map<String, Object> pattern = new LinkedHashMap<>();
		pattern.put("pattern", "^<(.+)> (.+)$");
		pattern.put("priority", 10);
		pattern.put("replaceMatched", false);
		pattern.put("placeholders", Map.of("player", "$1", "message", "$2"));

		Map<String, Object> payloadData = new LinkedHashMap<>();
		payloadData.put("patterns", List.of(pattern));

		MessageExtensions extensions = new MessageExtensions();
		MessageExtensionKey<MapMessageExtensionPayload> key =
				new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);
		extensions.put(key, new MapMessageExtensionPayload("interception", payloadData));
		extensionData.put(NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "chat.message"), extensions);

		service.uploadMessages(new MessageSnapshot(entries, filePaths, extensionData));

		Optional<MessageFileEntity> file = fileRepo.findByFilePathAndNamespace(
				Constants.Namespace.INTERNAL, "chat/message");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		List<MessageExtensionEntity> storedExtensions =
				extensionRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(1, storedExtensions.size());

		MessageExtensionEntity stored = storedExtensions.get(0);
		assertEquals("interception", stored.getExtensionId());

		Map<String, Object> decoded = MessageExtensionCodec.decode(stored.getPayload());
		assertTrue(decoded.containsKey("patterns"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUploadMessagesWithoutExtensions(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		MessageFileRepository fileRepo = fileRepo(type);
		MessageEntryRepository entryRepo = entryRepo(type);
		MessageExtensionRepository extensionRepo = extensionRepo(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("simple.message", template("Simple message"));
		filePaths.put("simple", resolveFile("simple/message.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));

		Optional<MessageFileEntity> file = fileRepo.findByFilePathAndNamespace(
				Constants.Namespace.INTERNAL, "simple/message");
		assertTrue(file.isPresent());

		Optional<MessageEntryEntity> entryEntity = entryRepo.findByFileIdAndEntryKey(file.get().getId(), "message");
		assertTrue(entryEntity.isPresent());

		List<MessageExtensionEntity> storedExtensions =
				extensionRepo.findAllByEntryId(entryEntity.get().getId());
		assertEquals(0, storedExtensions.size());
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
