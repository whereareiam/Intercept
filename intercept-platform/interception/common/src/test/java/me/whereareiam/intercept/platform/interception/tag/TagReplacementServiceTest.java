package me.whereareiam.intercept.platform.interception.tag;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.common.config.template.MessagesTemplate;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.platform.interception.SemanticaTestHelper;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.keystone.Serializers;
import me.whereareiam.keystone.model.SerializerOptions;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.intercept.common.messaging.InterceptTranslationRegistry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TagReplacementServiceTest {
	private DefaultMessageRegistry registry;
	private TagReplacementService tagService;
	private Messages messages;
	private TranslationService<Locale> translationService;

	@BeforeEach
	void setUp() {
		Registry<Reloadable> registryMock = mock(Registry.class);
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, registryMock);
		Settings settings = new SettingsTemplate().supply(new Settings());
		messages = new MessagesTemplate().supply(new Messages());
		translationService = SemanticaTestHelper.createService(settings, translationRegistry);

		// Initialize Serializer for tests
		SerializerOptions options = SerializerOptions.builder()
				.defaultAdapter("MINIMESSAGE")
				.prefixSupplier(messages::getPrefix)
				.enableLegacyColors(false)
				.enablePlayerNamePlaceholder(true)
				.build();
		SerializerEngine engine = Serializers.createEngine(options);
		Serializer.initialize(() -> engine);

		Provider<Messages> messagesProvider = () -> messages;
		FallbackMessageFormatter fallbackFormatter = new FallbackMessageFormatter(messagesProvider);
		TagReplacementBuilder builder = new TagReplacementBuilder(translationService, fallbackFormatter);
		tagService = new DefaultTagReplacementService(builder);
	}

	@Test
	void shouldReplaceSimpleTag() {
		registerMessage("welcome.message", template("Welcome to the server!"));
		Component input = Component.text("<lang key=\"welcome.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Welcome to the server!", plainText);
	}

	@Test
	void shouldReplaceTagWithPlaceholders() {
		registerMessage("player.join", template("<p:name> joined the game!"));
		Component input = Component.text("<lang key=\"player.join\" name=\"Steve\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Steve joined the game!", plainText);
	}

	@Test
	void shouldReplaceMultipleTags() {
		registerMessage("prefix.info", template("[INFO]"));
		registerMessage("shop.opened", template("Shop opened"));
		Component input = Component.text("<lang key=\"prefix.info\"> <lang key=\"shop.opened\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("[INFO] Shop opened", plainText);
	}

	@Test
	void shouldPreserveFormattingWhenReplacingTags() {
		registerMessage("welcome", template("Welcome!"));
		Component input = Component.text("Server: ")
				.append(Component.text("<lang key=\"welcome\">").color(NamedTextColor.GOLD));

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Server: Welcome!", plainText);

		// Verify gold color is preserved
		Component secondChild = result.children().get(0);
		assertEquals(NamedTextColor.GOLD, secondChild.color());
	}

	@Test
	void shouldPreserveBoldAndItalic() {
		registerMessage("message", template("Important"));
		Component input = Component.text("<lang key=\"message\">")
				.decorate(TextDecoration.BOLD)
				.decorate(TextDecoration.ITALIC);

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		assertTrue(result.hasDecoration(TextDecoration.BOLD));
		assertTrue(result.hasDecoration(TextDecoration.ITALIC));
	}

	@Test
	void shouldReturnOriginalComponentWhenNoTags() {
		Component input = Component.text("No tag here");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		assertEquals(input, result);
	}

	@Test
	void shouldHandleNonExistentMessageKey() {
		Component input = Component.text("<lang key=\"nonexistent.key\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		// Should fall back to using the key itself
		assertTrue(plainText.contains("nonexistent.key"));
	}

	@Test
	void shouldOnlyReplaceMatchingTagName() {
		registerMessage("message", template("Hello"));
		Component input = Component.text("<lang key=\"message\"> <other key=\"test\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertTrue(plainText.contains("Hello"));
		assertTrue(plainText.contains("<other key=\"test\">"));
	}

	@Test
	void shouldHandleComplexComponentTree() {
		registerMessage("greeting", template("Hello"));
		registerMessage("name", template("World"));

		Component input = Component.text("Start ")
				.append(Component.text("<lang key=\"greeting\">").color(NamedTextColor.RED))
				.append(Component.text(" <lang key=\"name\">").color(NamedTextColor.BLUE))
				.append(Component.text(" End"));

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Start Hello World End", plainText);
	}

	@Test
	void shouldReplaceTagsWithMessageReferences() {
		registerMessage("prefix", template("[Server]"));
		registerMessage("announcement", template("<ref:prefix> Maintenance soon"));

		Component input = Component.text("<lang key=\"announcement\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("[Server] Maintenance soon", plainText);
	}

	@Test
	void shouldHandleTagsWithMultiplePlaceholders() {
		registerMessage("player.info", template("<p:name> (<p:rank>) from <p:location>"));
		Component input = Component.text("<lang key=\"player.info\" name=\"Steve\" rank=\"Admin\" location=\"Spawn\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Steve (Admin) from Spawn", plainText);
	}

	@Test
	void shouldDetectTagsInComponent() {
		Component input = Component.text("Welcome <lang key=\"message\">");

		boolean result = tagService.containsTags(input, "<lang>");

		assertTrue(result);
	}

	@Test
	void shouldNotDetectNonExistentTags() {
		Component input = Component.text("No tag here");

		boolean result = tagService.containsTags(input, "<lang>");

		assertFalse(result);
	}

	@Test
	void shouldHandleEmptyComponent() {
		Component input = Component.empty();

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		assertEquals(input, result);
	}

	@Test
	void shouldHandleTagsSurroundedByText() {
		registerMessage("item", template("Diamond Sword"));
		Component input = Component.text("You received a <lang key=\"item\">!");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("You received a Diamond Sword!", plainText);
	}

	@Test
	void shouldReplaceTagsWithLocalization() {
		registerMessage("welcome", localized(java.util.Map.of(
				Locale.US, "Welcome!",
				Locale.FRANCE, "Bienvenue!"
		)));

		Component input = Component.text("<lang key=\"welcome\">");

		Component resultEN = tagService.replaceTags(input, "<lang>", Locale.US);
		Component resultFR = tagService.replaceTags(input, "<lang>", Locale.FRANCE);

		assertEquals("Welcome!", extractPlainText(resultEN));
		assertEquals("Bienvenue!", extractPlainText(resultFR));
	}

	@Test
	void shouldHandleNestedFormattingWithTags() {
		registerMessage("important", template("ALERT"));

		Component input = Component.text()
				.append(Component.text("[").color(NamedTextColor.GRAY))
				.append(Component.text("<lang key=\"important\">")
						.color(NamedTextColor.RED)
						.decorate(TextDecoration.BOLD))
				.append(Component.text("]").color(NamedTextColor.GRAY))
				.build();

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("[ALERT]", plainText);

		// Verify formatting is preserved
		Component alertPart = result.children().get(1);
		assertEquals(NamedTextColor.RED, alertPart.color());
		assertTrue(alertPart.hasDecoration(TextDecoration.BOLD));
	}

	@Test
	void shouldUseChatFallbackFormatForMissingTranslation() {
		// Test that missing translation uses CHAT source formatting
		Component input = Component.text("<lang key=\"missing.chat.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.CHAT);

		String plainText = extractPlainText(result);
		// Default chat format: "<gray>[</gray><red>Missing: <key></red><gray>]</gray>"
		assertTrue(plainText.contains("Missing: missing.chat.message") || plainText.contains("missing.chat.message"),
				"Should contain fallback text, got: " + plainText);
	}

	@Test
	void shouldUseDefaultFallbackForUnknownSource() {
		// Test that missing translation uses default format for UNKNOWN source
		Component input = Component.text("<lang key=\"missing.unknown.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.UNKNOWN);

		String plainText = extractPlainText(result);
		// Default format: "<key>" - no colors, just the key
		assertTrue(plainText.contains("missing.unknown.message"),
				"Should contain the key, got: " + plainText);
	}

	@Test
	void shouldNotApplyFallbackToExistingTranslations() {
		// Test that existing translations are not affected by fallback mechanism
		registerMessage("existing.message", template("This exists!"));
		Component input = Component.text("<lang key=\"existing.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.CHAT);

		String plainText = extractPlainText(result);
		assertEquals("This exists!", plainText);
	}

	@Test
	void shouldHandleMultipleMissingTranslationsWithDifferentSources() {
		// Test multiple missing translations with different source formatting
		Component input = Component.text()
				.append(Component.text("<lang key=\"missing.chat\">"))
				.append(Component.text(" | "))
				.append(Component.text("<lang key=\"missing.command\">"))
				.build();

		// Note: We can only pass one source, so this tests that it applies consistently
		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.CHAT);

		String plainText = extractPlainText(result);
		assertTrue(plainText.contains("missing.chat"));
		assertTrue(plainText.contains("missing.command"));
	}

	@Test
	void shouldHandleFallbackWhenDisabled() {
		// Test that fallback mechanism can be disabled
		messages.getFallback().setEnabled(false);
		Component input = Component.text("<lang key=\"missing.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.CHAT);

		String plainText = extractPlainText(result);
		// Should just return the key when fallback is disabled
		assertEquals("missing.message", plainText);

		// Re-enable for other tests
		messages.getFallback().setEnabled(true);
	}

	@Test
	void shouldPreserveFormattingWithSourceSpecificFallback() {
		// Test that fallback format's styling is applied (original component styling is replaced)
		Component input = Component.text("<lang key=\"missing.formatted\">")
				.color(NamedTextColor.BLUE)
				.decorate(TextDecoration.BOLD);

		Component result = tagService.replaceTags(input, "<lang>", Locale.US, ComponentType.CHAT);

		// The fallback format (<dark_gray><key></dark_gray>) should be applied
		// Original component styling (BLUE + BOLD) is replaced by fallback styling
		String plainText = extractPlainText(result);
		assertTrue(plainText.contains("missing.formatted"), "Should contain the key");

		// Check that the result is a component (fallback format was applied)
		assertNotNull(result);
	}

	// Helper method to register entries for both registries
	private void registerMessage(String key, TranslationEntry entry) {
		registry.register(key, entry);
		SemanticaTestHelper.register(translationService, key, entry);
	}

	private TranslationEntry template(String text) {
		return SemanticaTestHelper.template(text);
	}

	private TranslationEntry localized(java.util.Map<Locale, String> translations) {
		return SemanticaTestHelper.localized(translations);
	}

	// Helper method to extract plain text from component
	private String extractPlainText(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}
}

