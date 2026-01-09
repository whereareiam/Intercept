package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.util.NamespaceUtil;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
		String key = NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "errors.permissions.no-permission");
		assertTrue(snapshot.getEntries().containsKey(key));
		TranslationEntry entry = snapshot.getEntries().get(key);
		assertInstanceOf(TemplateEntry.class, entry);
		assertEquals("No permission", ((TemplateEntry) entry).getTemplate());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDownloadReturnsAllFilePaths(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);

		uploadMultipleEntries(service);

		MessageSnapshot snapshot = service.downloadMessages();

		assertEquals(2, snapshot.getFilePaths().size());
		assertEquals(resolveFile("errors/permissions.yml"),
				snapshot.getFilePaths().get(NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "errors.permissions")));
		assertEquals(resolveFile("common/greeting.yml"),
				snapshot.getFilePaths().get(NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "common.greeting")));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDownloadIncludesExtensions(DatabaseType type) {
		DefaultMessagePersistenceService service = service(type);

		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, Path> filePaths = new HashMap<>();
		Map<String, MessageExtensions> extensionData = new HashMap<>();

		entries.put("chat.message", new TemplateEntry("Chat message"));
		filePaths.put("chat", resolveFile("chat/message.yml"));

		Map<String, Object> pattern = new LinkedHashMap<>();
		pattern.put("pattern", "^<(.+)> (.+)$");
		pattern.put("priority", 10);
		pattern.put("replaceMatched", false);

		Map<String, Object> payloadData = new LinkedHashMap<>();
		payloadData.put("patterns", List.of(pattern));

		MessageExtensions extensions = new MessageExtensions();
		MessageExtensionKey<MapMessageExtensionPayload> key = new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);
		extensions.put(key, new MapMessageExtensionPayload("interception", payloadData));
		extensionData.put(NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "chat.message"), extensions);

		service.uploadMessages(new MessageSnapshot(entries, filePaths, extensionData));

		MessageSnapshot snapshot = service.downloadMessages();
		String fullKey = NamespaceUtil.qualify(Constants.Namespace.INTERNAL, "chat.message");
		assertTrue(snapshot.getExtensions().containsKey(fullKey));

		MessageExtensions loaded = snapshot.getExtensions().get(fullKey);
		assertNotNull(loaded);

		MapMessageExtensionPayload loadedPayload = loaded.get(key);
		assertNotNull(loadedPayload);
		assertTrue(loadedPayload.data().containsKey("patterns"));
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
