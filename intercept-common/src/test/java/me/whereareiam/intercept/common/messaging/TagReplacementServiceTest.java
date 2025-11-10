package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.type.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class TagReplacementServiceTest {
	private DefaultMessageRegistry registry;
	private TagReplacementService tagService;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry();
		Settings settings = new SettingsTemplate().supply(new Settings());
		MessageService messageService = new DefaultMessageService(registry, settings);
		tagService = new DefaultTagReplacementService(messageService);
	}

	@Test
	void shouldReplaceSimpleTag() {
		registry.register("welcome.message", new DefaultMessageEntry(MessageType.MESSAGE, "Welcome to the server!"));
		Component input = Component.text("<lang key=\"welcome.message\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Welcome to the server!", plainText);
	}

	@Test
	void shouldReplaceTagWithPlaceholders() {
		registry.register("player.join", new DefaultMessageEntry(MessageType.MESSAGE, "<p:name> joined the game!"));
		Component input = Component.text("<lang key=\"player.join\" name=\"Steve\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("Steve joined the game!", plainText);
	}

	@Test
	void shouldReplaceMultipleTags() {
		registry.register("prefix.info", new DefaultMessageEntry(MessageType.MESSAGE, "[INFO]"));
		registry.register("shop.opened", new DefaultMessageEntry(MessageType.MESSAGE, "Shop opened"));
		Component input = Component.text("<lang key=\"prefix.info\"> <lang key=\"shop.opened\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("[INFO] Shop opened", plainText);
	}

	@Test
	void shouldPreserveFormattingWhenReplacingTags() {
		registry.register("welcome", new DefaultMessageEntry(MessageType.MESSAGE, "Welcome!"));
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
		registry.register("message", new DefaultMessageEntry(MessageType.MESSAGE, "Important"));
		Component input = Component.text("<lang key=\"message\">")
				.decorate(TextDecoration.BOLD)
				.decorate(TextDecoration.ITALIC);

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		assertTrue(result.hasDecoration(TextDecoration.BOLD));
		assertTrue(result.hasDecoration(TextDecoration.ITALIC));
	}

	@Test
	void shouldReturnOriginalComponentWhenNoTags() {
		Component input = Component.text("No tags here");

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
		registry.register("message", new DefaultMessageEntry(MessageType.MESSAGE, "Hello"));
		Component input = Component.text("<lang key=\"message\"> <other key=\"test\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertTrue(plainText.contains("Hello"));
		assertTrue(plainText.contains("<other key=\"test\">"));
	}

	@Test
	void shouldHandleComplexComponentTree() {
		registry.register("greeting", new DefaultMessageEntry(MessageType.MESSAGE, "Hello"));
		registry.register("name", new DefaultMessageEntry(MessageType.MESSAGE, "World"));

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
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[Server]"));
		registry.register("announcement", new DefaultMessageEntry(MessageType.MESSAGE, "<m:prefix> Maintenance soon"));

		Component input = Component.text("<lang key=\"announcement\">");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("[Server] Maintenance soon", plainText);
	}

	@Test
	void shouldHandleTagsWithMultiplePlaceholders() {
		registry.register("player.info", new DefaultMessageEntry(
				MessageType.MESSAGE,
				"<p:name> (<p:rank>) from <p:location>"
		));
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
		Component input = Component.text("No tags here");

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
		registry.register("item", new DefaultMessageEntry(MessageType.MESSAGE, "Diamond Sword"));
		Component input = Component.text("You received a <lang key=\"item\">!");

		Component result = tagService.replaceTags(input, "<lang>", Locale.US);

		String plainText = extractPlainText(result);
		assertEquals("You received a Diamond Sword!", plainText);
	}

	@Test
	void shouldReplaceTagsWithLocalization() {
		DefaultMessageEntry entry = new DefaultMessageEntry(
				MessageType.MESSAGE,
				java.util.Map.of(
						"en_US", "Welcome!",
						"fr_FR", "Bienvenue!"
				)
		);
		registry.register("welcome", entry);

		Component input = Component.text("<lang key=\"welcome\">");

		Component resultEN = tagService.replaceTags(input, "<lang>", Locale.US);
		Component resultFR = tagService.replaceTags(input, "<lang>", Locale.FRANCE);

		assertEquals("Welcome!", extractPlainText(resultEN));
		assertEquals("Bienvenue!", extractPlainText(resultFR));
	}

	@Test
	void shouldHandleNestedFormattingWithTags() {
		registry.register("important", new DefaultMessageEntry(MessageType.MESSAGE, "ALERT"));

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

	// Helper method to extract plain text from component
	private String extractPlainText(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}
}
