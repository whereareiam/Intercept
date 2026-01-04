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
				() -> settings
		);
	}

	@Test
	void shouldLoadSingleLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of("Welcome!"));
		fileData.putEntry("welcome", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		TranslationEntry entry = registry.get("test.welcome");
		assertTrue(entry instanceof TemplateEntry);
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of("Welcome!"),
				"de_DE", MultiValue.of("Willkommen!")
		));
		fileData.putEntry("welcome", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		assertEquals("Willkommen!", translationService.resolve("test.welcome", Locale.GERMANY));
		TranslationEntry stored = registry.get("test.welcome");
		assertTrue(stored instanceof LocalizedEntry);
	}

	@Test
	void shouldLoadTemplates() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of("[Prefix]"));
		fileData.putEntry("prefix", entry);

		registerEntries(loader.loadFromData("templates", fileData));

		assertTrue(registry.exists("templates.prefix"));
		assertTrue(registry.get("templates.prefix") instanceof TemplateEntry);
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of(List.of("Line 1", "Line 2", "Line 3")));
		fileData.putEntry("banner", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertEquals("Line 1\nLine 2\nLine 3", translationService.resolve("test.banner", Locale.US));
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of(List.of("Line 1", "Line 2")),
				"de_DE", MultiValue.of(List.of("Zeile 1", "Zeile 2"))
		));
		fileData.putEntry("banner", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertEquals("Line 1\nLine 2", translationService.resolve("test.banner", Locale.US));
		assertEquals("Zeile 1\nZeile 2", translationService.resolve("test.banner", Locale.GERMANY));
	}

	@Test
	void shouldDetectMessageTypeFromLocales() {
		MessageDocument fileData = new MessageDocument();

		MessageDocument.Entry msgEntry = new MessageDocument.Entry();
		msgEntry.setLocales(Map.of("en_US", MultiValue.of("Text")));

		fileData.putEntry("msg", msgEntry);

		registerEntries(loader.loadFromData("test", fileData));

		assertTrue(registry.get("test.msg") instanceof LocalizedEntry);
	}

	@Test
	void shouldDetectTemplateTypeFromText() {
		MessageDocument fileData = new MessageDocument();

		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of("Text"));
		fileData.putEntry("tpl", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertTrue(registry.get("test.tpl") instanceof TemplateEntry);
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setLocales(Map.of(
				"en_US", MultiValue.of("Hello"),
				"de_DE", MultiValue.of("Hallo")
		));
		fileData.putEntry("greeting", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertEquals("Hello", translationService.resolve("test.greeting", Locale.US));
		assertEquals("Hallo", translationService.resolve("test.greeting", Locale.GERMANY));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry1 = new MessageDocument.Entry();
		entry1.setText(MultiValue.of("Message 1"));
		fileData.putEntry("msg1", entry1);

		MessageDocument.Entry entry2 = new MessageDocument.Entry();
		entry2.setText(MultiValue.of("Message 2"));
		fileData.putEntry("msg2", entry2);

		MessageDocument.Entry entry3 = new MessageDocument.Entry();
		entry3.setText(MultiValue.of("Message 3"));
		fileData.putEntry("msg3", entry3);

		registerEntries(loader.loadFromData("test", fileData));

		assertEquals(3, registry.getKeys("test").size());
	}

	private void registerEntries(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return;
		translationService.register(entries);
	}
}
