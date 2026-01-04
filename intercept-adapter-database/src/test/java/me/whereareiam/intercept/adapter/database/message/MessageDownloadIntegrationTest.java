package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDownloadIntegrationTest extends BaseMessagePersistenceIntegrationTest {

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDownloadWritesFilesToDisk(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);
		Path filePath = resolveFile("errors/permissions.yml");

		uploadSingleEntry(service);

		MessageSnapshot snapshot = service.downloadMessages();

		assertTrue(Files.exists(filePath), "Downloaded file should exist on disk");
		assertEquals(1, snapshot.getEntries().size());
		assertTrue(snapshot.getEntries().containsKey("errors.permissions.no-permission"));
		TranslationEntry entry = snapshot.getEntries().get("errors.permissions.no-permission");
		assertTrue(entry instanceof TemplateEntry);
		assertEquals("No permission", ((TemplateEntry) entry).getTemplate());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDownloadReturnsAllFilePaths(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);

		uploadMultipleEntries(service);

		MessageSnapshot snapshot = service.downloadMessages();

		assertEquals(2, snapshot.getFilePaths().size());
		assertEquals(resolveFile("errors/permissions.yml"), snapshot.getFilePaths().get("errors.permissions"));
		assertEquals(resolveFile("common/greeting.yml"), snapshot.getFilePaths().get("common.greeting"));
	}

	private void uploadSingleEntry(DefaultMessagePersistenceService service) {
		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("errors.permissions.no-permission", new TemplateEntry("No permission"));
		filePaths.put("errors.permissions", resolveFile("errors/permissions.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));
	}

	private void uploadMultipleEntries(DefaultMessagePersistenceService service) {
		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();

		entries.put("errors.permissions.no-permission", new TemplateEntry("No permission"));
		entries.put("common.greeting", new TemplateEntry("Hello <player>!"));
		filePaths.put("errors.permissions", resolveFile("errors/permissions.yml"));
		filePaths.put("common", resolveFile("common/greeting.yml"));

		service.uploadMessages(new MessageSnapshot(entries, filePaths));
	}
}
