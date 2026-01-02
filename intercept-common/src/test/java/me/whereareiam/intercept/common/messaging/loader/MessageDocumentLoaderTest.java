package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.SemanticaTestHelper;
import me.whereareiam.intercept.common.messaging.interception.DefaultInterceptionRegistry;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.semantica.translation.TranslationService;
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
		Settings settings = new SettingsTemplate().supply(new Settings());
		TranslationService<Locale> translationService = SemanticaTestHelper.createService(settings);
		InterceptionRegistry interceptionRegistry = new DefaultInterceptionRegistry();
		loader = new DefaultMessageFileLoader(
				registry,
				interceptionRegistry,
				new TextProcessor(),
				translationService,
				() -> settings
		);
	}

	@Test
	void shouldLoadSingleLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("welcome", "Welcome!");

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText());
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", "Welcome!",
				"de_DE", "Willkommen!"
		));
		fileData.putEntry("welcome", entry);

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", registry.get("test.welcome").getText(Locale.US));
		assertEquals("Willkommen!", registry.get("test.welcome").getText(Locale.GERMANY));
	}

	@Test
	void shouldLoadTemplates() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("prefix", "[Prefix]");

		loader.loadFromData("templates", fileData);

		assertTrue(registry.exists("templates.prefix"));
		assertEquals(MessageType.TEMPLATE, registry.get("templates.prefix").getType());
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("banner", List.of("Line 1", "Line 2", "Line 3"));

		loader.loadFromData("test", fileData);

		String text = registry.get("test.banner").getText();
		assertEquals("Line 1\nLine 2\nLine 3", text);
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", List.of("Line 1", "Line 2"),
				"de_DE", List.of("Zeile 1", "Zeile 2")
		));
		fileData.putEntry("banner", entry);

		loader.loadFromData("test", fileData);

		assertEquals("Line 1\nLine 2", registry.get("test.banner").getText(Locale.US));
		assertEquals("Zeile 1\nZeile 2", registry.get("test.banner").getText(Locale.GERMANY));
	}

	@Test
	void shouldDetectMessageTypeFromLocales() {
		MessageDocument fileData = new MessageDocument();

		MessageDocumentEntry msgEntry = new MessageDocumentEntry();
		msgEntry.setLocales(Map.of("en_US", "Text"));

		fileData.putEntry("msg", msgEntry);

		loader.loadFromData("test", fileData);

		assertEquals(MessageType.MESSAGE, registry.get("test.msg").getType());
	}

	@Test
	void shouldDetectTemplateTypeFromText() {
		MessageDocument fileData = new MessageDocument();

		fileData.putEntry("tpl", "Text");

		loader.loadFromData("test", fileData);

		assertEquals(MessageType.TEMPLATE, registry.get("test.tpl").getType());
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", "Hello",
				"de_DE", "Hallo"
		));
		fileData.putEntry("greeting", entry);

		loader.loadFromData("test", fileData);

		assertEquals("Hello", registry.get("test.greeting").getText(Locale.US));
		assertEquals("Hallo", registry.get("test.greeting").getText(Locale.GERMANY));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("msg1", "Message 1");
		fileData.putEntry("msg2", "Message 2");
		fileData.putEntry("msg3", "Message 3");

		loader.loadFromData("test", fileData);

		assertEquals(3, registry.getKeysByPrefix("test").size());
	}
}
