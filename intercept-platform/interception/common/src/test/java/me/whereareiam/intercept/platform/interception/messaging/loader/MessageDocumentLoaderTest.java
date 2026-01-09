package me.whereareiam.intercept.platform.interception.messaging.loader;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.registry.InterceptTranslationRegistry;
import me.whereareiam.intercept.platform.interception.SemanticaTestHelper;
import me.whereareiam.intercept.platform.interception.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.platform.interception.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageDocumentLoaderTest {
	@Mock
	private Registry<Reloadable> reloadableRegistry;

	private MessageFileLoader loader;
	private DefaultMessageRegistry registry;
	private TranslationService<Locale> translationService;

	@BeforeEach
	void setUp() {
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, reloadableRegistry);
		Settings settings = new SettingsTemplate().supply(new Settings());
		translationService = SemanticaTestHelper.createService(settings, translationRegistry);
		loader = new DefaultMessageFileLoader(
				new TextProcessor(),
				() -> Locale.US
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
		assertInstanceOf(TemplateEntry.class, registry.get("test.welcome"));
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
		assertInstanceOf(LocalizedEntry.class, stored);
	}

	@Test
	void shouldLoadTemplates() {
		MessageDocument fileData = new MessageDocument();
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of("[Prefix]"));
		fileData.putEntry("prefix", entry);

		registerEntries(loader.loadFromData("templates", fileData));

		assertTrue(registry.exists("templates.prefix"));
		assertInstanceOf(TemplateEntry.class, registry.get("templates.prefix"));
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

		assertInstanceOf(LocalizedEntry.class, registry.get("test.msg"));
	}

	@Test
	void shouldDetectTemplateTypeFromText() {
		MessageDocument fileData = new MessageDocument();

		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of("Text"));
		fileData.putEntry("tpl", entry);

		registerEntries(loader.loadFromData("test", fileData));

		assertInstanceOf(TemplateEntry.class, registry.get("test.tpl"));
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
		entry1.setText(MultiValue.of("Namespace 1"));
		fileData.putEntry("msg1", entry1);

		MessageDocument.Entry entry2 = new MessageDocument.Entry();
		entry2.setText(MultiValue.of("Namespace 2"));
		fileData.putEntry("msg2", entry2);

		MessageDocument.Entry entry3 = new MessageDocument.Entry();
		entry3.setText(MultiValue.of("Namespace 3"));
		fileData.putEntry("msg3", entry3);

		registerEntries(loader.loadFromData("test", fileData));

		assertEquals(3, registry.getKeys("test").size());
	}

	private void registerEntries(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return;
		translationService.register(entries);
	}
}
