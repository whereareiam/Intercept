package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MessageDocumentLoaderTest {
	private MessageFileLoader loader;
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry(mock(Registry.class));
		loader = new DefaultMessageFileLoader(registry, new TextProcessor());
	}

	@Test
	void shouldLoadSingleLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setText("Welcome!");

		fileData.setItems(Map.of("welcome", entry));

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText());
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setTranslations(Map.of(
				"en_US", "Welcome!",
				"de_DE", "Willkommen!"
		));

		fileData.setItems(Map.of("welcome", entry));

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText(Locale.US));
		assertEquals("Willkommen!", registry.get("test.welcome").getText(Locale.GERMANY));
	}

	@Test
	void shouldLoadTemplates() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.TEMPLATE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setText("[Prefix]");

		fileData.setItems(Map.of("prefix", entry));

		loader.loadFromData("templates", fileData);

		assertTrue(registry.exists("templates.prefix"));
		assertEquals(MessageType.TEMPLATE, registry.get("templates.prefix").getType());
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setText(List.of("Line 1", "Line 2", "Line 3"));

		fileData.setItems(Map.of("banner", entry));

		loader.loadFromData("test", fileData);

		String text = registry.get("test.banner").getText();
		assertEquals("Line 1\nLine 2\nLine 3", text);
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setTranslations(Map.of(
				"en_US", List.of("Line 1", "Line 2"),
				"de_DE", List.of("Zeile 1", "Zeile 2")
		));

		fileData.setItems(Map.of("banner", entry));

		loader.loadFromData("test", fileData);

		assertEquals("Line 1\nLine 2", registry.get("test.banner").getText(Locale.US));
		assertEquals("Zeile 1\nZeile 2", registry.get("test.banner").getText(Locale.GERMANY));
	}

	@Test
	void shouldInheritTypeFromFile() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.TEMPLATE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		// No type set on entry
		entry.setText("Template text");

		fileData.setItems(Map.of("tpl", entry));

		loader.loadFromData("templates", fileData);

		assertEquals(MessageType.TEMPLATE, registry.get("templates.tpl").getType());
	}

	@Test
	void shouldOverrideFileType() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setType(MessageType.TEMPLATE); // Override
		entry.setText("Template text");

		fileData.setItems(Map.of("tpl", entry));

		loader.loadFromData("test", fileData);

		assertEquals(MessageType.TEMPLATE, registry.get("test.tpl").getType());
	}

	@Test
	void shouldAutoDetectMessageType() {
		MessageDocument fileData = new MessageDocument();
		// No persistence-level type

		MessageDocumentEntry msgEntry = new MessageDocumentEntry();
		msgEntry.setTranslations(Map.of("en_US", "Text"));

		MessageDocumentEntry tplEntry = new MessageDocumentEntry();
		tplEntry.setText("Text");

		fileData.setItems(Map.of("msg", msgEntry, "tpl", tplEntry));

		loader.loadFromData("test", fileData);

		// Should auto-detect: translations = MESSAGE, text only = TEMPLATE
		assertEquals(MessageType.MESSAGE, registry.get("test.msg").getType());
		assertEquals(MessageType.TEMPLATE, registry.get("test.tpl").getType());
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setTranslations(Map.of(
				"en_US", "Hello",
				"de_DE", "Hallo"
		));

		fileData.setItems(Map.of("greeting", entry));

		loader.loadFromData("test", fileData);

		assertEquals("Hello", registry.get("test.greeting").getText(Locale.US));
		assertEquals("Hallo", registry.get("test.greeting").getText(Locale.GERMANY));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageDocument fileData = new MessageDocument();
		fileData.setType(MessageType.MESSAGE);

		MessageDocumentEntry entry1 = new MessageDocumentEntry();
		entry1.setText("Message 1");

		MessageDocumentEntry entry2 = new MessageDocumentEntry();
		entry2.setText("Message 2");

		MessageDocumentEntry entry3 = new MessageDocumentEntry();
		entry3.setText("Message 3");

		fileData.setItems(Map.of(
				"msg1", entry1,
				"msg2", entry2,
				"msg3", entry3
		));

		loader.loadFromData("test", fileData);

		assertEquals(3, registry.getKeysByPrefix("test").size());
	}
}