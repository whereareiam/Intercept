package me.whereareiam.intercept.common.messaging.processor;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderProcessorTest {
	private final PlaceholderProcessor processor = new PlaceholderProcessor();

	@Test
	void shouldReplaceSimplePlaceholder() {
		String text = "Welcome, <p:name>!";
		String result = processor.process(text, Map.of("name", "Steve"));
		assertEquals("Welcome, Steve!", result);
	}

	@Test
	void shouldReplaceMultiplePlaceholders() {
		String text = "Player <p:player> has <p:balance> coins";
		String result = processor.process(text, Map.of(
				"player", "Steve",
				"balance", "100"
		));
		assertEquals("Player Steve has 100 coins", result);
	}

	@Test
	void shouldKeepOriginalWhenPlaceholderMissing() {
		String text = "Welcome, <p:name>!";
		String result = processor.process(text, Map.of());
		assertEquals("Welcome, <p:name>!", result);
	}

	@Test
	void shouldHandlePlaceholderWithSpecialCharacters() {
		String text = "Message: <p:msg>";
		String result = processor.process(text, Map.of("msg", "Hello & <world>"));
		assertEquals("Message: Hello & <world>", result);
	}

	@Test
	void shouldHandlePlaceholderWithMiniMessageTags() {
		String text = "Text: <p:content>";
		String result = processor.process(text, Map.of("content", "<red>Error</red>"));
		assertEquals("Text: <red>Error</red>", result);
	}

	@Test
	void shouldHandleEmptyPlaceholderValue() {
		String text = "Value: <p:val>";
		String result = processor.process(text, Map.of("val", ""));
		assertEquals("Value: ", result);
	}

	@Test
	void shouldHandleNullPlaceholderValue() {
		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("val", null);
		String text = "Value: <p:val>";
		String result = processor.process(text, placeholders);
		assertEquals("Value: ", result);
	}

	@Test
	void shouldHandleTextWithNoPlaceholders() {
		String text = "Just plain text";
		String result = processor.process(text, Map.of());
		assertEquals("Just plain text", result);
	}

	@Test
	void shouldHandlePlaceholderWithUnderscore() {
		String text = "Player: <p:player_name>";
		String result = processor.process(text, Map.of("player_name", "Test_User"));
		assertEquals("Player: Test_User", result);
	}

	@Test
	void shouldHandlePlaceholderWithNumbers() {
		String text = "Value: <p:value123>";
		String result = processor.process(text, Map.of("value123", "456"));
		assertEquals("Value: 456", result);
	}

	@Test
	void shouldHandleNumericPlaceholderValue() {
		String text = "Balance: <p:amount>";
		String result = processor.process(text, Map.of("amount", 1000));
		assertEquals("Balance: 1000", result);
	}

	@Test
	void shouldHandleBooleanPlaceholderValue() {
		String text = "Online: <p:status>";
		String result = processor.process(text, Map.of("status", true));
		assertEquals("Online: true", result);
	}

	@Test
	void shouldHandleRepeatedPlaceholder() {
		String text = "<p:name> said hello to <p:name>";
		String result = processor.process(text, Map.of("name", "Steve"));
		assertEquals("Steve said hello to Steve", result);
	}

	@Test
	void shouldHandleMultilinePlaceholder() {
		String text = "Message:\n<p:msg>";
		String result = processor.process(text, Map.of("msg", "Line1\nLine2"));
		assertEquals("Message:\nLine1\nLine2", result);
	}

	@Test
	void shouldNotProcessOtherTags() {
		String text = "<m:prefix> <p:name> <if condition>";
		String result = processor.process(text, Map.of("name", "Steve"));
		assertEquals("<m:prefix> Steve <if condition>", result);
	}
}