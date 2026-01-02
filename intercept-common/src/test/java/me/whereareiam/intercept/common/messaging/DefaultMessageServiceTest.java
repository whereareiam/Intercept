package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DefaultMessageServiceTest {
	private DefaultMessageRegistry registry;
	private TranslationService<Locale> service;

	@BeforeEach
	void setUp() {
		Registry<Reloadable> mockRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(mockRegistry);
		Settings settings = new SettingsTemplate().supply(new Settings());
		service = SemanticaTestHelper.createService(settings);
	}

	@Test
	void shouldResolveSimpleMessage() {
		registerMessage("welcome", new CompiledMessageEntry(MessageType.MESSAGE, "Welcome!"));

		String result = service.resolve("welcome", Locale.US);
		assertEquals("Welcome!", result);
	}

	@Test
	void shouldResolveMessageWithPlaceholder() {
		registerMessage("greeting", new CompiledMessageEntry(MessageType.MESSAGE, "Hello, <p:name>!"));

		String result = service.resolve("greeting", Locale.US, Map.of("name", "Steve"));
		assertEquals("Hello, Steve!", result);
	}

	@Test
	void shouldResolveMessageWithReference() {
		registerMessage("prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "[App]"));
		registerMessage("message", new CompiledMessageEntry(MessageType.MESSAGE, "<m:prefix> Hello"));

		String result = service.resolve("message", Locale.US);
		assertEquals("[App] Hello", result);
	}

	@Test
	void shouldResolveMessageWithTemplate() {
		registerMessage("error.fmt", new CompiledMessageEntry(MessageType.TEMPLATE, "ERROR: <p:msg>"));
		registerMessage("error", new CompiledMessageEntry(MessageType.MESSAGE, "<m:error.fmt msg='Failed'>"));

		String result = service.resolve("error", Locale.US);
		assertEquals("ERROR: Failed", result);
	}

	@Test
	void shouldResolveMessageWithConditional() {
		registerMessage("status", new CompiledMessageEntry(MessageType.MESSAGE,
				"Player is <if online==true>online<else>offline</if>"));

		String result = service.resolve("status", Locale.US, Map.of("online", true));
		assertEquals("Player is online", result);
	}

	@Test
	void shouldResolveComplexMessage() {
		// Setup
		registerMessage("prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "[<p:app>]"));
		registerMessage("color", new CompiledMessageEntry(MessageType.TEMPLATE, "<red>"));
		registerMessage("error.fmt", new CompiledMessageEntry(MessageType.TEMPLATE,
				"<m:color>ERROR: <p:message>"));
		registerMessage("error", new CompiledMessageEntry(MessageType.MESSAGE,
				"<m:prefix app='System'> <m:error.fmt message='<p:details>'>"));

		String result = service.resolve("error", Locale.US, Map.of("details", "Connection lost"));
		assertEquals("[System] <red>ERROR: Connection lost", result);
	}

	@Test
	void shouldResolveMultiLocaleMessage() {
		registerMessage("welcome", new CompiledMessageEntry(MessageType.MESSAGE,
				Map.of(Locale.US, "Welcome!", Locale.GERMAN, "Willkommen!")));

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
		registerMessage("test", new CompiledMessageEntry(MessageType.MESSAGE, "Test"));

		assertTrue(service.exists("test"));
		assertFalse(service.exists("missing"));
	}

	@Test
	void shouldGetAvailableLocales() {
		registerMessage("msg", new CompiledMessageEntry(MessageType.MESSAGE,
				Map.of(Locale.US, "Hello", Locale.GERMAN, "Hallo", Locale.FRANCE, "Bonjour")));

		var locales = service.getAvailableLocales("msg");
		assertEquals(3, locales.size());
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.US)));
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.GERMAN)));
		assertTrue(locales.contains(SemanticLocale.wrap(Locale.FRANCE)));
	}

	@Test
	void shouldUseUpdatedMessageAfterRegister() {
		registerMessage("cached", new CompiledMessageEntry(MessageType.MESSAGE, "Original"));

		assertEquals("Original", service.resolve("cached", Locale.US));

		registerMessage("cached", new CompiledMessageEntry(MessageType.MESSAGE, "Updated"));

		assertEquals("Updated", service.resolve("cached", Locale.US));
	}

	private void registerMessage(String key, CompiledMessageEntry entry) {
		registry.register(key, entry);
		SemanticaTestHelper.register(service, key, entry);
	}
}
