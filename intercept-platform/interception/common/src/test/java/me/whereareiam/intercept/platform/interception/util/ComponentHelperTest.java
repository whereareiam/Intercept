package me.whereareiam.intercept.platform.interception.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

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
	void shouldReplaceTextInSimpleComponent() {
		Component component = Component.text("Hello <tag>");
		Map<String, Component> replacements = Map.of("<tag>", Component.text("World"));
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals("Hello World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldReplaceTextWhilePreservingFormatting() {
		Component component = Component.text("Welcome ")
				.append(Component.text("<lang key=\"message\">").color(NamedTextColor.GOLD));
		Map<String, Component> replacements = Map.of("<lang key=\"message\">", Component.text("to the server"));
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals("Welcome to the server", ComponentHelper.extractPlainText(result));
		
		// Verify formatting is preserved (gold color on replaced text)
		Component secondChild = result.children().get(0);
		assertEquals(NamedTextColor.GOLD, secondChild.color());
	}

	@Test
	void shouldReplaceMultipleTagsInComponent() {
		Component component = Component.text("<tag1> and <tag2>");
		Map<String, Component> replacements = Map.of(
				"<tag1>", Component.text("Hello"),
				"<tag2>", Component.text("World")
		);
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals("Hello and World", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldPreserveBoldAndItalic() {
		Component component = Component.text("Hello <tag>")
				.decorate(TextDecoration.BOLD)
				.decorate(TextDecoration.ITALIC);
		Map<String, Component> replacements = Map.of("<tag>", Component.text("World"));
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
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
		Map<String, Component> replacements = Map.of(
				"<tag1>", Component.text("Hello"),
				"<tag2>", Component.text("World")
		);
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals("Start Hello World End", ComponentHelper.extractPlainText(result));
	}

	@Test
	void shouldReturnOriginalComponentWhenNoReplacements() {
		Component component = Component.text("Hello World");
		Map<String, Component> replacements = Map.of();
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals(component, result);
	}

	@Test
	void shouldHandlePartialTagReplacement() {
		Component component = Component.text("Prefix <lang key=\"msg\"> Suffix");
		Map<String, Component> replacements = Map.of("<lang key=\"msg\">", Component.text("Content"));
		
		Component result = ComponentHelper.replaceTextWithComponents(component, replacements);
		
		assertEquals("Prefix Content Suffix", ComponentHelper.extractPlainText(result));
	}

}

