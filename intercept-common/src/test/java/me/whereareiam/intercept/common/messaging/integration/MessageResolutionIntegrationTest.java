package me.whereareiam.intercept.common.messaging.integration;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.DefaultMessageService;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for the complete message resolution pipeline.
 */
class MessageResolutionIntegrationTest {
	private DefaultMessageRegistry registry;
	private MessageService service;

	@BeforeEach
	void setUp() {
		Registry<Reloadable> registryMock = mock(Registry.class);
		registry = new DefaultMessageRegistry(registryMock);
		Settings settings = new SettingsTemplate().supply(new Settings());
		Registry<Reloadable> reloadables = mock(Registry.class);
		service = new DefaultMessageService(registry, settings, reloadables);
	}

	@Test
	void shouldResolveCompleteMessageSystem() {
		// Setup color palette
		registry.register("colors.primary", new CompiledMessageEntry(MessageType.TEMPLATE, "<#5DADE2>"));
		registry.register("colors.error", new CompiledMessageEntry(MessageType.TEMPLATE, "<red>"));
		registry.register("colors.success", new CompiledMessageEntry(MessageType.TEMPLATE, "<green>"));

		// Setup style templates
		registry.register("styles.prefix", new CompiledMessageEntry(MessageType.TEMPLATE,
				"<m:colors.primary>[Intercept]<reset>"));
		registry.register("styles.player.name", new CompiledMessageEntry(MessageType.TEMPLATE,
				"<m:colors.primary><p:name><reset>"));
		registry.register("styles.error.format", new CompiledMessageEntry(MessageType.TEMPLATE,
				"<m:colors.error>✗ <p:message>"));

		// Setup messages
		registry.register("errors.no.permission", new CompiledMessageEntry(MessageType.MESSAGE,
				Map.of(
						Locale.US, "<m:styles.prefix> <tpl:styles.error.format message='You lack permission: <p:permission>'>",
						Locale.GERMAN, "<m:styles.prefix> <tpl:styles.error.format message='Keine Berechtigung: <p:permission>'>"
				)));

		// Resolve in English
		String enResult = service.resolve("errors.no.permission", Locale.US,
				Map.of("permission", "intercept.admin"));
		assertEquals("<#5DADE2>[Intercept]<reset> <red>✗ You lack permission: intercept.admin", enResult);

		// Resolve in German
		String deResult = service.resolve("errors.no.permission", Locale.GERMANY,
				Map.of("permission", "intercept.admin"));
		assertEquals("<#5DADE2>[Intercept]<reset> <red>✗ Keine Berechtigung: intercept.admin", deResult);
	}

	@Test
	void shouldResolveComplexConditionalMessage() {
		registry.register("prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "[Server]"));
		registry.register("player.status", new CompiledMessageEntry(MessageType.MESSAGE,
				"<m:prefix> Player <p:player> is <if online==true><green>online<else><red>offline</if><if online==true> on server <p:server></if>"));

		// Online player
		String onlineResult = service.resolve("player.status", Locale.US,
				Map.of("player", "Steve", "online", true, "server", "lobby"));
		assertEquals("[Server] Player Steve is <green>online on server lobby", onlineResult);

		// Offline player
		String offlineResult = service.resolve("player.status", Locale.US,
				Map.of("player", "Alex", "online", false));
		assertEquals("[Server] Player Alex is <red>offline", offlineResult);
	}

	@Test
	void shouldResolveNestedTemplates() {
		registry.register("base.color", new CompiledMessageEntry(MessageType.TEMPLATE, "<yellow>"));
		registry.register("wrapper", new CompiledMessageEntry(MessageType.TEMPLATE, "[<m:base.color><p:content>]"));
		registry.register("message", new CompiledMessageEntry(MessageType.MESSAGE,
				"<tpl:wrapper content='Important'>"));

		String result = service.resolve("message", Locale.US);
		assertEquals("[<yellow>Important]", result);
	}

	@Test
	void shouldResolveMultipleConditionsAndPlaceholders() {
		registry.register("complex", new CompiledMessageEntry(MessageType.MESSAGE,
				"<if rank==admin><red>[Admin]<else><if rank==mod><blue>[Mod]<else><gray>[Player]</if></if> <p:name>: <p:message>"));

		String adminResult = service.resolve("complex", Locale.US,
				Map.of("rank", "admin", "name", "Steve", "message", "Hello"));
		assertEquals("<red>[Admin] Steve: Hello", adminResult);

		String modResult = service.resolve("complex", Locale.US,
				Map.of("rank", "mod", "name", "Alex", "message", "Hi"));
		assertEquals("<blue>[Mod] Alex: Hi", modResult);

		String playerResult = service.resolve("complex", Locale.US,
				Map.of("rank", "player", "name", "Bob", "message", "Hey"));
		assertEquals("<gray>[Player] Bob: Hey", playerResult);
	}

	@Test
	void shouldHandleEmptyConditionals() {
		registry.register("vip.welcome", new CompiledMessageEntry(MessageType.MESSAGE,
				"<if vip==true><gold>[VIP] </if>Welcome, <p:name>!"));

		String vipResult = service.resolve("vip.welcome", Locale.US,
				Map.of("vip", true, "name", "Steve"));
		assertEquals("<gold>[VIP] Welcome, Steve!", vipResult);

		String normalResult = service.resolve("vip.welcome", Locale.US,
				Map.of("vip", false, "name", "Alex"));
		assertEquals("Welcome, Alex!", normalResult);
	}

	@Test
	void shouldResolveChainedReferences() {
		registry.register("a", new CompiledMessageEntry(MessageType.TEMPLATE, "A"));
		registry.register("b", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:a>B"));
		registry.register("c", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>C"));
		registry.register("final", new CompiledMessageEntry(MessageType.MESSAGE, "Value: <m:c>"));

		String result = service.resolve("final", Locale.US);
		assertEquals("Value: ABC", result);
	}

	@Test
	void shouldHandleMissingPlaceholderGracefully() {
		registry.register("msg", new CompiledMessageEntry(MessageType.MESSAGE,
				"Hello, <p:name>! Balance: <p:balance>"));

		// Only provide one placeholder
		String result = service.resolve("msg", Locale.US, Map.of("name", "Steve"));
		assertEquals("Hello, Steve! Balance: <p:balance>", result);
	}

	@Test
	void shouldHandleComplexTemplateParameters() {
		registry.register("box", new CompiledMessageEntry(MessageType.TEMPLATE,
				"╔═══╗\n║ <p:title> ║\n║ <p:message> ║\n╚═══╗"));
		registry.register("error", new CompiledMessageEntry(MessageType.MESSAGE,
				"<tpl:box title='Error' message='Something went wrong'>"));

		String result = service.resolve("error", Locale.US);
		assertEquals("╔═══╗\n║ Error ║\n║ Something went wrong ║\n╚═══╗", result);
	}

	@Test
	void shouldResolveNumericConditions() {
		registry.register("health.status", new CompiledMessageEntry(MessageType.MESSAGE,
				"Health: <if health>50><green>Good<else><red>Low</if> (<p:health>/100)"));

		String goodHealth = service.resolve("health.status", Locale.US, Map.of("health", 75));
		assertEquals("Health: <green>Good (75/100)", goodHealth);

		String lowHealth = service.resolve("health.status", Locale.US, Map.of("health", 25));
		assertEquals("Health: <red>Low (25/100)", lowHealth);
	}
}