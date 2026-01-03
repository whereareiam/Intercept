package me.whereareiam.intercept.common.messaging.persistence;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for writing message documents back to disk.
 */
class FileWritingIntegrationTest {
	@TempDir
	Path tempDir;

	private MessageFileWriter writer;

	@BeforeEach
	void setUp() {
		Config.setWriter(Config.writer(Format.YAML));
		Config.setReader(Config.reader(Format.YAML));
		writer = new DefaultMessageFileWriter(tempDir);
	}

	@Test
	void shouldWriteDocumentWithNestedPath() {
		MessageDocument document = new MessageDocument();

		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("text", "Welcome!");

		Map<String, Object> regex = new LinkedHashMap<>();
		regex.put("pattern", ".*hello.*");
		regex.put("priority", 5);
		regex.put("replaceMatched", true);
		Map<String, Object> placeholders = new LinkedHashMap<>();
		placeholders.put("name", "$1");
		regex.put("placeholders", placeholders);
		List<Map<String, Object>> patterns = new java.util.ArrayList<>();
		patterns.add(regex);
		Map<String, Object> interception = new LinkedHashMap<>();
		interception.put("patterns", patterns);
		entry.put("interception", interception);

		document.putEntry("welcome", entry);

		String relativePath = "errors/permissions";
		writer.write(relativePath, document);

		Path writtenPath = writer.resolvePath(relativePath);
		assertTrue(Files.exists(writtenPath), "Expected file to be written to disk");

		@SuppressWarnings("unchecked")
		Map<String, Object> loaded = (Map<String, Object>) Config.load(writtenPath, Map.class);
		assertTrue(loaded.containsKey("welcome"));
		Object loadedEntry = loaded.get("welcome");
		assertTrue(loadedEntry instanceof Map);
		Map<?, ?> loadedMap = (Map<?, ?>) loadedEntry;
		assertEquals("Welcome!", loadedMap.get("text"));
		Object interceptionRaw = loadedMap.get("interception");
		assertTrue(interceptionRaw instanceof Map);
		Map<?, ?> interceptionMap = (Map<?, ?>) interceptionRaw;
		Object patternsRaw = interceptionMap.get("patterns");
		assertTrue(patternsRaw instanceof List);
		assertEquals(1, ((List<?>) patternsRaw).size());
	}
}
