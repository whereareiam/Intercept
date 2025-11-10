package me.whereareiam.intercept.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentHelperTest {

	@Test
	void shouldExtractPlainTextFromSimpleComponent() {
		Component component = Component.text("Hello World");
		
		String result = ComponentHelper.extractPlainText(component);
		
		assertEquals("Hello World", result);
	}

	@Test
	void shouldExtractPlainTextFromComponentWithFormatting() {
		Component component = Component.text("Hello ")
				.color(NamedTextColor.RED)
				.append(Component.text("World").color(NamedTextColor.BLUE));
		
		String result = ComponentHelper.extractPlainText(component);
		
		assertEquals("Hello World", result);
	}

	@Test
	void shouldDetectTagInComponent() {
		Component component = Component.text("Welcome <lang key=\"message\">");
		
		boolean result = ComponentHelper.containsTag(component, "<lang>");
		
		assertTrue(result);
	}

	@Test
	void shouldNotDetectMissingTag() {
		Component component = Component.text("Welcome to the server");
		
		boolean result = ComponentHelper.containsTag(component, "<lang>");
		
		assertFalse(result);
	}

	@Test
	void shouldExtractSimpleTag() {
		String text = "Welcome <lang key=\"welcome.message\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("<lang key=\"welcome.message\">", tag.getOriginalTag());
		assertEquals("welcome.message", tag.getKey());
		assertTrue(tag.getPlaceholders().isEmpty());
	}

	@Test
	void shouldExtractTagWithPlaceholders() {
		String text = "<lang key=\"player.join\" name=\"Steve\" rank=\"Admin\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("<lang key=\"player.join\" name=\"Steve\" rank=\"Admin\">", tag.getOriginalTag());
		assertEquals("player.join", tag.getKey());
		assertEquals(2, tag.getPlaceholders().size());
		
		ComponentHelper.TagData.Placeholder name = tag.getPlaceholders().get(0);
		assertEquals("name", name.getName());
		assertEquals("Steve", name.getValue());
		
		ComponentHelper.TagData.Placeholder rank = tag.getPlaceholders().get(1);
		assertEquals("rank", rank.getName());
		assertEquals("Admin", rank.getValue());
	}

	@Test
	void shouldExtractMultipleTags() {
		String text = "<lang key=\"prefix\"> Welcome <lang key=\"player.join\" name=\"Steve\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(2, tags.size());
		assertEquals("prefix", tags.get(0).getKey());
		assertEquals("player.join", tags.get(1).getKey());
	}

	@Test
	void shouldIgnoreTagsWithoutKeyAttribute() {
		String text = "<lang name=\"Steve\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertTrue(tags.isEmpty());
	}

	@Test
	void shouldExtractTagWithEmptyValue() {
		String text = "<lang key=\"message\" param=\"\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("message", tag.getKey());
		assertEquals(1, tag.getPlaceholders().size());
		assertEquals("", tag.getPlaceholders().get(0).getValue());
	}

	@Test
	void shouldNotExtractTagsForDifferentTagName() {
		String text = "<lang key=\"message\"> <other key=\"test\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "translate");
		
		assertTrue(tags.isEmpty());
	}

	@Test
	void shouldReplaceTextInSimpleComponent() {
		Component component = Component.text("Hello <tag>");
		Map<String, String> replacements = Map.of("<tag>", "World");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Hello World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldReplaceTextWhilePreservingFormatting() {
		Component component = Component.text("Welcome ")
				.append(Component.text("<lang key=\"message\">").color(NamedTextColor.GOLD));
		Map<String, String> replacements = Map.of("<lang key=\"message\">", "to the server");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Welcome to the server", ComponentHelper.extractPlainText(result));
		
		// Verify formatting is preserved (gold color on replaced text)
		Component secondChild = result.children().get(0);
		assertEquals(NamedTextColor.GOLD, secondChild.color());
	}

	@Test
	void shouldReplaceMultipleTagsInComponent() {
		Component component = Component.text("<tag1> and <tag2>");
		Map<String, String> replacements = Map.of(
				"<tag1>", "Hello",
				"<tag2>", "World"
		);
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Hello and World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldPreserveBoldAndItalic() {
		Component component = Component.text("Hello <tag>")
				.decorate(TextDecoration.BOLD)
				.decorate(TextDecoration.ITALIC);
		Map<String, String> replacements = Map.of("<tag>", "World");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Hello World", ComponentHelper.extractPlainText(result));
		assertTrue(result.hasDecoration(TextDecoration.BOLD));
		assertTrue(result.hasDecoration(TextDecoration.ITALIC));
	}

	@Test
	void shouldHandleNestedComponents() {
		Component component = Component.text("Start ")
				.append(Component.text("<tag1>").color(NamedTextColor.RED)
						.append(Component.text(" <tag2>").color(NamedTextColor.BLUE)))
				.append(Component.text(" End"));
		Map<String, String> replacements = Map.of(
				"<tag1>", "Hello",
				"<tag2>", "World"
		);
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Start Hello World End", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldReturnOriginalComponentWhenNoReplacements() {
		Component component = Component.text("Hello World");
		Map<String, String> replacements = Map.of();
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals(component, result);
	}

	@Test
	void shouldHandlePartialTagReplacement() {
		Component component = Component.text("Prefix <lang key=\"msg\"> Suffix");
		Map<String, String> replacements = Map.of("<lang key=\"msg\">", "Content");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Prefix Content Suffix", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldExtractTagWithSpecialCharactersInValue() {
		String text = "<lang key=\"message\" text=\"Hello, World! (123)\">";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("message", tag.getKey());
		assertEquals(1, tag.getPlaceholders().size());
		assertEquals("Hello, World! (123)", tag.getPlaceholders().get(0).getValue());
	}

	@Test
	void shouldHandleTagsWithMultipleSpaces() {
		String text = "<lang  key=\"message\"  name=\"Steve\"  >";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "<lang>");
		
		assertEquals(1, tags.size());
		assertEquals("message", tags.get(0).getKey());
	}

	// Tests for different tag formats

	@Test
	void shouldExtractTagWithSquareBrackets() {
		String text = "Welcome [l key=\"welcome.message\"]";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "[l]");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("[l key=\"welcome.message\"]", tag.getOriginalTag());
		assertEquals("welcome.message", tag.getKey());
		assertTrue(tag.getPlaceholders().isEmpty());
	}

	@Test
	void shouldExtractTagWithCurlyBraces() {
		String text = "Hello {tr key=\"player.join\" name=\"Steve\"}";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "{tr}");
		
		assertEquals(1, tags.size());
		ComponentHelper.TagData tag = tags.get(0);
		assertEquals("{tr key=\"player.join\" name=\"Steve\"}", tag.getOriginalTag());
		assertEquals("player.join", tag.getKey());
		assertEquals(1, tag.getPlaceholders().size());
		assertEquals("name", tag.getPlaceholders().get(0).getName());
		assertEquals("Steve", tag.getPlaceholders().get(0).getValue());
	}

	@Test
	void shouldDetectSquareBracketTagInComponent() {
		Component component = Component.text("Welcome [l key=\"message\"]");
		
		boolean result = ComponentHelper.containsTag(component, "[l]");
		
		assertTrue(result);
	}

	@Test
	void shouldDetectCurlyBraceTagInComponent() {
		Component component = Component.text("Welcome {tr key=\"message\"}");
		
		boolean result = ComponentHelper.containsTag(component, "{tr}");
		
		assertTrue(result);
	}

	@Test
	void shouldNotDetectWrongTagFormat() {
		Component component = Component.text("Welcome <lang key=\"message\">");
		
		boolean resultSquare = ComponentHelper.containsTag(component, "[l]");
		boolean resultCurly = ComponentHelper.containsTag(component, "{tr}");
		
		assertFalse(resultSquare);
		assertFalse(resultCurly);
	}

	@Test
	void shouldReplaceSquareBracketTags() {
		Component component = Component.text("Hello [l key=\"msg\"]");
		Map<String, String> replacements = Map.of("[l key=\"msg\"]", "World");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Hello World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldReplaceCurlyBraceTags() {
		Component component = Component.text("Hello {tr key=\"msg\"}");
		Map<String, String> replacements = Map.of("{tr key=\"msg\"}", "World");
		
		Component result = ComponentHelper.replaceTextInComponent(component, replacements);
		
		assertEquals("Hello World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldExtractMultipleTagsWithDifferentFormat() {
		String text = "[l key=\"first\"] and [l key=\"second\" name=\"Steve\"]";
		
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(text, "[l]");
		
		assertEquals(2, tags.size());
		assertEquals("first", tags.get(0).getKey());
		assertEquals("second", tags.get(1).getKey());
		assertEquals(1, tags.get(1).getPlaceholders().size());
	}

	@Test
	void shouldHandleInvalidTagFormat() {
		String text = "Welcome <lang key=\"message\">";
		
		// Invalid format (too short)
		List<ComponentHelper.TagData> tags1 = ComponentHelper.extractTags(text, "l");
		assertTrue(tags1.isEmpty());
		
		// Invalid format (just opening)
		List<ComponentHelper.TagData> tags2 = ComponentHelper.extractTags(text, "<>");
		assertTrue(tags2.isEmpty());
		
		// Null format
		List<ComponentHelper.TagData> tags3 = ComponentHelper.extractTags(text, null);
		assertTrue(tags3.isEmpty());
	}
}

