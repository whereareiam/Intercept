package me.whereareiam.intercept.platform.interception.messaging.persistence;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for writing message documents back to disk.
 */
class FileWritingIntegrationTest {
	private static final MessageExtensionKey<MapMessageExtensionPayload> INTERCEPTION_KEY = new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);

	@TempDir
	Path tempDir;

	private MessageFileWriter writer;

	@BeforeEach
	void setUp() {
		Config.setWriter(Config.writer(Format.YAML));
		Config.setReader(Config.reader(Format.YAML));
		Config.registerAdapter(MessageDocument.Node.class, new MessageDocumentNodeAdapter());
		writer = new InterceptionMessageFileWriter(tempDir);
	}

	@Test
	void shouldWriteDocumentWithNestedPath() {
		MessageFileData document = new MessageFileData();

		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.text("Welcome!"));

		Map<String, Object> regex = new java.util.LinkedHashMap<>();
		regex.put("pattern", ".*hello.*");
		regex.put("priority", 5);
		regex.put("replaceMatched", true);
		regex.put("placeholders", Map.of("name", "$1"));

		MapMessageExtensionPayload interception = new MapMessageExtensionPayload(
				"interception",
				Map.of("patterns", List.of(regex))
		);
		MessageExtensions extensions = new MessageExtensions();
		extensions.put(INTERCEPTION_KEY, interception);
		entry.setExtensions(extensions);

		document.putEntry("welcome", entry);

		String relativePath = "errors/permissions";
		writer.write(relativePath, document);

		Path writtenPath = writer.resolvePath(relativePath);
		assertTrue(Files.exists(writtenPath), "Expected file to be written to disk");

		@SuppressWarnings("unchecked")
		Map<String, Object> loaded = (Map<String, Object>) Config.load(writtenPath, Map.class);
		assertTrue(loaded.containsKey("welcome"));
		Object loadedEntry = loaded.get("welcome");
		assertInstanceOf(Map.class, loadedEntry);
		Map<?, ?> loadedMap = (Map<?, ?>) loadedEntry;
		assertEquals("Welcome!", loadedMap.get("text"));
		Object interceptionRaw = loadedMap.get("interception");
		assertInstanceOf(Map.class, interceptionRaw);
		Map<?, ?> interceptionMap = (Map<?, ?>) interceptionRaw;
		Object patternsRaw = interceptionMap.get("patterns");
		assertInstanceOf(List.class, patternsRaw);
		assertEquals(1, ((List<?>) patternsRaw).size());
	}
}
