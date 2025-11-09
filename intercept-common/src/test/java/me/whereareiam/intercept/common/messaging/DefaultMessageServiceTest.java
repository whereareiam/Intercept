package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.MessageRequest;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMessageServiceTest {
	private DefaultMessageRegistry registry;
	private MessageService service;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry();
		Settings settings = new SettingsTemplate().supply(new Settings());
		service = new DefaultMessageService(registry, settings);
	}

	@Test
	void shouldResolveSimpleMessage() {
		registry.register("welcome", new DefaultMessageEntry(MessageType.MESSAGE, "Welcome!"));

		String result = service.resolve("welcome", Locale.US);
		assertEquals("Welcome!", result);
	}

	@Test
	void shouldResolveMessageWithPlaceholder() {
		registry.register("greeting", new DefaultMessageEntry(MessageType.MESSAGE, "Hello, <p:name>!"));

		String result = service.resolve("greeting", Locale.US, Map.of("name", "Steve"));
		assertEquals("Hello, Steve!", result);
	}

	@Test
	void shouldResolveMessageWithReference() {
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[App]"));
		registry.register("message", new DefaultMessageEntry(MessageType.MESSAGE, "<m:prefix> Hello"));

		String result = service.resolve("message", Locale.US);
		assertEquals("[App] Hello", result);
	}

	@Test
	void shouldResolveMessageWithTemplate() {
		registry.register("error-fmt", new DefaultMessageEntry(MessageType.TEMPLATE, "ERROR: <p:msg>"));
		registry.register("error", new DefaultMessageEntry(MessageType.MESSAGE, "<tpl:error-fmt msg='Failed'>"));

		String result = service.resolve("error", Locale.US);
		assertEquals("ERROR: Failed", result);
	}

	@Test
	void shouldResolveMessageWithConditional() {
		registry.register("status", new DefaultMessageEntry(MessageType.MESSAGE,
				"Player is <if online==true>online<else>offline</if>"));

		String result = service.resolve("status", Locale.US, Map.of("online", true));
		assertEquals("Player is online", result);
	}

	@Test
	void shouldResolveComplexMessage() {
		// Setup
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[<p:app>]"));
		registry.register("color", new DefaultMessageEntry(MessageType.TEMPLATE, "<red>"));
		registry.register("error-fmt", new DefaultMessageEntry(MessageType.TEMPLATE,
				"<m:color>ERROR: <p:message>"));
		registry.register("error", new DefaultMessageEntry(MessageType.MESSAGE,
				"<tpl:prefix app='System'> <tpl:error-fmt message='<p:details>'>"));

		String result = service.resolve("error", Locale.US, Map.of("details", "Connection lost"));
		assertEquals("[System] <red>ERROR: Connection lost", result);
	}

	@Test
	void shouldResolveMultiLocaleMessage() {
		registry.register("welcome", new DefaultMessageEntry(MessageType.MESSAGE,
				Map.of("en_US", "Welcome!", "de_DE", "Willkommen!")));

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
		registry.register("test", new DefaultMessageEntry(MessageType.MESSAGE, "Test"));

		assertTrue(service.exists("test"));
		assertFalse(service.exists("missing"));
	}

	@Test
	void shouldGetAvailableLocales() {
		registry.register("msg", new DefaultMessageEntry(MessageType.MESSAGE,
				Map.of("en_US", "Hello", "de_DE", "Hallo", "fr_FR", "Bonjour")));

		var locales = service.getAvailableLocales("msg");
		assertEquals(3, locales.size());
		assertTrue(locales.contains("en_US"));
		assertTrue(locales.contains("de_DE"));
		assertTrue(locales.contains("fr_FR"));
	}

	@Test
	void shouldResolveWithMessageRequest() {
		registry.register("msg", new DefaultMessageEntry(MessageType.MESSAGE, "Hello, <p:name>!"));

		MessageRequest request = MessageRequest.builder()
				.key("msg")
				.locale(Locale.US)
				.placeholders(Map.of("name", "Alice"))
				.build();

		String result = service.resolve(request);
		assertEquals("Hello, Alice!", result);
	}
}