package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageFileLoaderTest {
	private MessageFileLoader loader;
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry();
		TextProcessor textProcessor = new TextProcessor();
		loader = new MessageFileLoader(textProcessor, registry);
	}

	@Test
	void shouldLoadSingleLanguageMessage() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setText("Welcome!");

		fileData.setEntries(Map.of("welcome", entry));

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText());
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setTranslations(Map.of(
				"en_US", "Welcome!",
				"de_DE", "Willkommen!"
		));

		fileData.setEntries(Map.of("welcome", entry));

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText("en_US"));
		assertEquals("Willkommen!", registry.get("test.welcome").getText("de_DE"));
	}

	@Test
	void shouldLoadTemplates() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.TEMPLATE);

		MessageEntryData entry = new MessageEntryData();
		entry.setText("[Prefix]");

		fileData.setEntries(Map.of("prefix", entry));

		loader.loadFromData("templates", fileData);

		assertTrue(registry.exists("templates.prefix"));
		assertEquals(MessageType.TEMPLATE, registry.get("templates.prefix").getType());
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setText(List.of("Line 1", "Line 2", "Line 3"));

		fileData.setEntries(Map.of("banner", entry));

		loader.loadFromData("test", fileData);

		String text = registry.get("test.banner").getText();
		assertEquals("Line 1\nLine 2\nLine 3", text);
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setTranslations(Map.of(
				"en_US", List.of("Line 1", "Line 2"),
				"de_DE", List.of("Zeile 1", "Zeile 2")
		));

		fileData.setEntries(Map.of("banner", entry));

		loader.loadFromData("test", fileData);

		assertEquals("Line 1\nLine 2", registry.get("test.banner").getText("en_US"));
		assertEquals("Zeile 1\nZeile 2", registry.get("test.banner").getText("de_DE"));
	}

	@Test
	void shouldInheritTypeFromFile() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.TEMPLATE);

		MessageEntryData entry = new MessageEntryData();
		// No type set on entry
		entry.setText("Template text");

		fileData.setEntries(Map.of("tpl", entry));

		loader.loadFromData("templates", fileData);

		assertEquals(MessageType.TEMPLATE, registry.get("templates.tpl").getType());
	}

	@Test
	void shouldOverrideFileType() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setType(MessageType.TEMPLATE); // Override
		entry.setText("Template text");

		fileData.setEntries(Map.of("tpl", entry));

		loader.loadFromData("test", fileData);

		assertEquals(MessageType.TEMPLATE, registry.get("test.tpl").getType());
	}

	@Test
	void shouldAutoDetectMessageType() {
		MessageFileData fileData = new MessageFileData();
		// No file-level type

		MessageEntryData msgEntry = new MessageEntryData();
		msgEntry.setTranslations(Map.of("en_US", "Text"));

		MessageEntryData tplEntry = new MessageEntryData();
		tplEntry.setText("Text");

		fileData.setEntries(Map.of("msg", msgEntry, "tpl", tplEntry));

		loader.loadFromData("test", fileData);

		// Should auto-detect: translations = MESSAGE, text only = TEMPLATE
		assertEquals(MessageType.MESSAGE, registry.get("test.msg").getType());
		assertEquals(MessageType.TEMPLATE, registry.get("test.tpl").getType());
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry = new MessageEntryData();
		entry.setTranslations(Map.of(
				"en_US", "Hello",
				"de_DE", "Hallo"
		));

		fileData.setEntries(Map.of("greeting", entry));

		loader.loadFromData("test", fileData);

		assertEquals("Hello", registry.get("test.greeting").getText("en_US"));
		assertEquals("Hallo", registry.get("test.greeting").getText("de_DE"));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageFileData fileData = new MessageFileData();
		fileData.setType(MessageType.MESSAGE);

		MessageEntryData entry1 = new MessageEntryData();
		entry1.setText("Message 1");

		MessageEntryData entry2 = new MessageEntryData();
		entry2.setText("Message 2");

		MessageEntryData entry3 = new MessageEntryData();
		entry3.setText("Message 3");

		fileData.setEntries(Map.of(
				"msg1", entry1,
				"msg2", entry2,
				"msg3", entry3
		));

		loader.loadFromData("test", fileData);

		assertEquals(3, registry.getKeysByPrefix("test").size());
	}
}