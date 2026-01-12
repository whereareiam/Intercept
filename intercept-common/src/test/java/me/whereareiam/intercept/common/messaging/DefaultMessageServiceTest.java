package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.SemanticaTestHelper;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.registry.InterceptTranslationRegistry;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultMessageServiceTest {
	@Mock
	private Registry<Reloadable> reloadableRegistry;

	private DefaultMessageRegistry registry;
	private TranslationService<Locale> service;

	@BeforeEach
	void setUp() {
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, reloadableRegistry);
		Settings settings = new SettingsTemplate().supply(new Settings());
		service = SemanticaTestHelper.createService(settings, translationRegistry);
	}

	@Test
	void shouldResolveSimpleMessage() {
		registerMessage("welcome", SemanticaTestHelper.template("Welcome!"));

		String result = service.resolve("welcome", Locale.US);
		assertEquals("Welcome!", result);
	}

	@Test
	void shouldResolveMessageWithPlaceholder() {
		registerMessage("greeting", SemanticaTestHelper.template("Hello, <p:name>!"));

		String result = service.resolve("greeting", Locale.US, Map.of("name", "Steve"));
		assertEquals("Hello, Steve!", result);
	}

	@Test
	void shouldResolveMessageWithReference() {
		registerMessage("prefix", SemanticaTestHelper.template("[App]"));
		registerMessage("message", SemanticaTestHelper.template("<ref:prefix> Hello"));

		String result = service.resolve("message", Locale.US);
		assertEquals("[App] Hello", result);
	}

	@Test
	void shouldResolveMessageWithTemplate() {
		registerMessage("error.fmt", SemanticaTestHelper.template("ERROR: <p:msg>"));
		registerMessage("error", SemanticaTestHelper.template("<ref:error.fmt msg='Failed'>"));

		String result = service.resolve("error", Locale.US);
		assertEquals("ERROR: Failed", result);
	}

	@Test
	void shouldResolveMessageWithConditional() {
		registerMessage("status", SemanticaTestHelper.template(
				"Player is <if online==true>online<else>offline</if>"));

		String result = service.resolve("status", Locale.US, Map.of("online", true));
		assertEquals("Player is online", result);
	}

	@Test
	void shouldResolveComplexMessage() {
		// Setup
		registerMessage("prefix", SemanticaTestHelper.template("[<p:app>]"));
		registerMessage("color", SemanticaTestHelper.template("<red>"));
		registerMessage("error.fmt", SemanticaTestHelper.template(
				"<ref:color>ERROR: <p:message>"));
		registerMessage("error", SemanticaTestHelper.template(
				"<ref:prefix app='System'> <ref:error.fmt message='<p:details>'>"));

		String result = service.resolve("error", Locale.US, Map.of("details", "Connection lost"));
		assertEquals("[System] <red>ERROR: Connection lost", result);
	}

	@Test
	void shouldResolveMultiLocaleMessage() {
		registerMessage("welcome", SemanticaTestHelper.localized(
				Map.of(Locale.US, "Welcome!", Locale.GERMANY, "Willkommen!")));

		String enResult = service.resolve("welcome", Locale.US);
		assertEquals("Welcome!", enResult);

		String deResult = service.resolve("welcome", Locale.GERMANY);
		assertEquals("Willkommen!", deResult);
	}

	@Test
	void shouldReturnKeyForMissingEntry() {
		String result = service.resolve("nonexistent", Locale.US);
		assertEquals("nonexistent", result); // Returns key as fallback
	}

	@Test
	void shouldCheckIfKeyExists() {
		registerMessage("test", SemanticaTestHelper.template("Test"));

		assertTrue(service.exists("test"));
		assertFalse(service.exists("missing"));
	}

	@Test
	void shouldGetAvailableLocales() {
		registerMessage("msg", SemanticaTestHelper.localized(
				Map.of(Locale.US, "Hello", Locale.GERMANY, "Hallo", Locale.FRANCE, "Bonjour")));

		var locales = service.getAvailableLocales("msg");
		assertEquals(3, locales.size());
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.US)));
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.GERMANY)));
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.FRANCE)));
	}

	@Test
	void shouldUseUpdatedMessageAfterRegister() {
		registerMessage("cached", SemanticaTestHelper.template("Original"));

		assertEquals("Original", service.resolve("cached", Locale.US));

		registerMessage("cached", SemanticaTestHelper.template("Updated"));

		assertEquals("Updated", service.resolve("cached", Locale.US));
	}

	private void registerMessage(String key, TranslationEntry entry) {
		registry.register(key, entry);
		SemanticaTestHelper.register(service, key, entry);
	}
}
