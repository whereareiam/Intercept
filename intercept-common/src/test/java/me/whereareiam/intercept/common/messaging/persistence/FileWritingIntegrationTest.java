package me.whereareiam.intercept.common.messaging.persistence;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.type.message.MessageType;
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
		document.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setText("Welcome!");

		MessageDocumentRegex regex = new MessageDocumentRegex();
		regex.setPattern(".*hello.*");
		regex.setPriority(5);
		regex.setReplaceMatched(true);
		regex.setPlaceholders(Map.of("name", "$1"));
		entry.setRegex(List.of(regex));

		document.setItems(new LinkedHashMap<>(Map.of("welcome", entry)));

		String relativePath = "errors/permissions";
		writer.write(relativePath, document);

		Path writtenPath = writer.resolvePath(relativePath);
		assertTrue(Files.exists(writtenPath), "Expected file to be written to disk");

		MessageDocument loaded = Config.load(writtenPath, MessageDocument.class);
		assertEquals(MessageType.MESSAGE, loaded.getType());
		assertTrue(loaded.getItems().containsKey("welcome"));
		assertEquals("Welcome!", loaded.getItems().get("welcome").getText());
		assertEquals(1, loaded.getItems().get("welcome").getRegex().size());
	}
}