package me.whereareiam.intercept.common.messaging.processor;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextProcessorTest {
	private final TextProcessor processor = new TextProcessor();

	@Test
	void shouldProcessSingleString() {
		String result = processor.process("Hello, world!");
		assertEquals("Hello, world!", result);
	}

	@Test
	void shouldProcessArrayOfStrings() {
		List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
		String result = processor.process(lines);
		assertEquals("Line 1\nLine 2\nLine 3", result);
	}

	@Test
	void shouldHandleEmptyString() {
		String result = processor.process("");
		assertEquals("", result);
	}

	@Test
	void shouldHandleEmptyArray() {
		String result = processor.process(Collections.emptyList());
		assertEquals("", result);
	}

	@Test
	void shouldHandleSingleElementArray() {
		List<String> lines = List.of("Single line");
		String result = processor.process(lines);
		assertEquals("Single line", result);
	}

	@Test
	void shouldHandleNullValue() {
		assertThrows(IllegalArgumentException.class, () -> processor.process(null));
	}

	@Test
	void shouldRejectInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> processor.process(123));
	}

	@Test
	void shouldHandleArrayWithEmptyStrings() {
		List<String> lines = Arrays.asList("", "Line 2", "");
		String result = processor.process(lines);
		assertEquals("\nLine 2\n", result);
	}

	@Test
	void shouldHandleMultilineWithSpecialCharacters() {
		List<String> lines = Arrays.asList("╔════╗", "║ Hi ║", "╚════╝");
		String result = processor.process(lines);
		assertEquals("╔════╗\n║ Hi ║\n╚════╝", result);
	}
}