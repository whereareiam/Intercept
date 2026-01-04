package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.InterceptTranslationRegistry;
import me.whereareiam.intercept.common.SemanticaTestHelper;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
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
	private TranslationService<Locale> translationService;

	@BeforeEach
	void setUp() {
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, mock(Registry.class));
		Settings settings = new SettingsTemplate().supply(new Settings());
		translationService = SemanticaTestHelper.createService(settings, translationRegistry);
		loader = new DefaultMessageFileLoader(
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
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		TranslationEntry entry = registry.get("test.welcome");
		assertTrue(entry instanceof TemplateEntry);
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of("Welcome!"),
				"de_DE", MultiValue.of("Willkommen!")
		));
		fileData.putEntry("welcome", entry);

		loader.loadFromData("test", fileData);

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		assertEquals("Willkommen!", translationService.resolve("test.welcome", Locale.GERMANY));
		TranslationEntry stored = registry.get("test.welcome");
		assertTrue(stored instanceof LocalizedEntry);
	}

	@Test
	void shouldLoadTemplates() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("prefix", "[Prefix]");

		loader.loadFromData("templates", fileData);

		assertTrue(registry.exists("templates.prefix"));
		assertTrue(registry.get("templates.prefix") instanceof TemplateEntry);
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("banner", List.of("Line 1", "Line 2", "Line 3"));

		loader.loadFromData("test", fileData);

		assertEquals("Line 1\nLine 2\nLine 3", translationService.resolve("test.banner", Locale.US));
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of(List.of("Line 1", "Line 2")),
				"de_DE", MultiValue.of(List.of("Zeile 1", "Zeile 2"))
		));
		fileData.putEntry("banner", entry);

		loader.loadFromData("test", fileData);

		assertEquals("Line 1\nLine 2", translationService.resolve("test.banner", Locale.US));
		assertEquals("Zeile 1\nZeile 2", translationService.resolve("test.banner", Locale.GERMANY));
	}

	@Test
	void shouldDetectMessageTypeFromLocales() {
		MessageDocument fileData = new MessageDocument();

		MessageDocumentEntry msgEntry = new MessageDocumentEntry();
		msgEntry.setLocales(Map.of("en_US", MultiValue.of("Text")));

		fileData.putEntry("msg", msgEntry);

		loader.loadFromData("test", fileData);

		assertTrue(registry.get("test.msg") instanceof LocalizedEntry);
	}

	@Test
	void shouldDetectTemplateTypeFromText() {
		MessageDocument fileData = new MessageDocument();

		fileData.putEntry("tpl", "Text");

		loader.loadFromData("test", fileData);

		assertTrue(registry.get("test.tpl") instanceof TemplateEntry);
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocumentEntry entry = new MessageDocumentEntry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of("Hello"),
				"de_DE", MultiValue.of("Hallo")
		));
		fileData.putEntry("greeting", entry);

		loader.loadFromData("test", fileData);

		assertEquals("Hello", translationService.resolve("test.greeting", Locale.US));
		assertEquals("Hallo", translationService.resolve("test.greeting", Locale.GERMANY));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageDocument fileData = new MessageDocument();
		fileData.putEntry("msg1", "Message 1");
		fileData.putEntry("msg2", "Message 2");
		fileData.putEntry("msg3", "Message 3");

		loader.loadFromData("test", fileData);

		assertEquals(3, registry.getKeys("test").size());
	}
}
