package me.whereareiam.intercept.common.messaging.persistence;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentInterception;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
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

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setText(MultiValue.of("Welcome!"));

		MessageDocumentRegex regex = new MessageDocumentRegex();
		regex.setPattern(".*hello.*");
		regex.setPriority(5);
		regex.setReplaceMatched(true);
		regex.setPlaceholders(Map.of("name", "$1"));

		MessageDocumentInterception interception = new MessageDocumentInterception();
		interception.setPatterns(List.of(regex));
		entry.setInterception(interception);

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
