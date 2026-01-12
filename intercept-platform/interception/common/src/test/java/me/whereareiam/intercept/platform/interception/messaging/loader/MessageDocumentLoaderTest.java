package me.whereareiam.intercept.platform.interception.messaging.loader;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.registry.InterceptTranslationRegistry;
import me.whereareiam.intercept.platform.interception.SemanticaTestHelper;
import me.whereareiam.intercept.common.translation.loader.mapper.TranslationEntryMapper;
import me.whereareiam.intercept.common.translation.loader.mapper.TextProcessor;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
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

	private TranslationEntryMapper entryMapper;
	private DefaultMessageRegistry registry;
	private TranslationService<Locale> translationService;

	@BeforeEach
	void setUp() {
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, reloadableRegistry);
		Settings settings = new SettingsTemplate().supply(new Settings());
		translationService = SemanticaTestHelper.createService(settings, translationRegistry);
		entryMapper = new TranslationEntryMapper(new TextProcessor(), () -> Locale.US);
	}

	@Test
	void shouldLoadSingleLanguageMessage() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.text("Welcome!"));
		fileData.putEntry("welcome", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		assertInstanceOf(TemplateEntry.class, registry.get("test.welcome"));
	}

	@Test
	void shouldLoadMultiLanguageMessage() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setLocales(Map.of(
				"en_US", MessageValue.text("Welcome!"),
				"de_DE", MessageValue.text("Willkommen!")
		));
		fileData.putEntry("welcome", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertTrue(registry.exists("test.welcome"));
		assertEquals("Welcome!", translationService.resolve("test.welcome", Locale.US));
		assertEquals("Willkommen!", translationService.resolve("test.welcome", Locale.GERMANY));
		TranslationEntry stored = registry.get("test.welcome");
		assertInstanceOf(LocalizedEntry.class, stored);
	}

	@Test
	void shouldLoadTemplates() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.text("[Prefix]"));
		fileData.putEntry("prefix", entry);

		registerEntries(entryMapper.mapEntries("templates", fileData));

		assertTrue(registry.exists("templates.prefix"));
		assertInstanceOf(TemplateEntry.class, registry.get("templates.prefix"));
	}

	@Test
	void shouldConvertArrayToMultiLineText() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.lines(List.of("Line 1", "Line 2", "Line 3")));
		fileData.putEntry("banner", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertEquals("Line 1\nLine 2\nLine 3", translationService.resolve("test.banner", Locale.US));
	}

	@Test
	void shouldConvertArrayTranslations() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setLocales(Map.of(
				"en_US", MessageValue.lines(List.of("Line 1", "Line 2")),
				"de_DE", MessageValue.lines(List.of("Zeile 1", "Zeile 2"))
		));
		fileData.putEntry("banner", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertEquals("Line 1\nLine 2", translationService.resolve("test.banner", Locale.US));
		assertEquals("Zeile 1\nZeile 2", translationService.resolve("test.banner", Locale.GERMANY));
	}

	@Test
	void shouldDetectMessageTypeFromLocales() {
		MessageFileData fileData = new MessageFileData();

		MessageFileData.Entry msgEntry = new MessageFileData.Entry();
		msgEntry.setLocales(Map.of("en_US", MessageValue.text("Text")));

		fileData.putEntry("msg", msgEntry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertInstanceOf(LocalizedEntry.class, registry.get("test.msg"));
	}

	@Test
	void shouldDetectTemplateTypeFromText() {
		MessageFileData fileData = new MessageFileData();

		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.text("Text"));
		fileData.putEntry("tpl", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertInstanceOf(TemplateEntry.class, registry.get("test.tpl"));
	}

	@Test
	void shouldHandleMultipleTranslations() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setLocales(Map.of(
				"en_US", MessageValue.text("Hello"),
				"de_DE", MessageValue.text("Hallo")
		));
		fileData.putEntry("greeting", entry);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertEquals("Hello", translationService.resolve("test.greeting", Locale.US));
		assertEquals("Hallo", translationService.resolve("test.greeting", Locale.GERMANY));
	}

	@Test
	void shouldLoadMultipleEntries() {
		MessageFileData fileData = new MessageFileData();
		MessageFileData.Entry entry1 = new MessageFileData.Entry();
		entry1.setText(MessageValue.text("Namespace 1"));
		fileData.putEntry("msg1", entry1);

		MessageFileData.Entry entry2 = new MessageFileData.Entry();
		entry2.setText(MessageValue.text("Namespace 2"));
		fileData.putEntry("msg2", entry2);

		MessageFileData.Entry entry3 = new MessageFileData.Entry();
		entry3.setText(MessageValue.text("Namespace 3"));
		fileData.putEntry("msg3", entry3);

		registerEntries(entryMapper.mapEntries("test", fileData));

		assertEquals(3, registry.getKeys("test").size());
	}

	private void registerEntries(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return;
		translationService.register(entries);
	}
}
