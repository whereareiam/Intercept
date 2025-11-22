package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.messaging.DefaultMessageEntry;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MessageReferenceProcessorTest {
	private DefaultMessageRegistry registry;
	private MessageReferenceProcessor processor;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry(mock(Registry.class));
		processor = new MessageReferenceProcessor(registry);
	}

	@Test
	void shouldResolveSimpleReference() {
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[Intercept]"));

		String text = "<m:prefix> Hello!";
		String result = processor.process(text, Locale.US);
		assertEquals("[Intercept] Hello!", result);
	}

	@Test
	void shouldResolveMultipleReferences() {
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[Intercept]"));
		registry.register("suffix", new DefaultMessageEntry(MessageType.TEMPLATE, "Thanks!"));

		String text = "<m:prefix> Message <m:suffix>";
		String result = processor.process(text, Locale.US);
		assertEquals("[Intercept] Message Thanks!", result);
	}

	@Test
	void shouldResolveNestedReferences() {
		registry.register("color", new DefaultMessageEntry(MessageType.TEMPLATE, "<red>"));
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:color>[Intercept]"));

		String text = "<m:prefix> Hello!";
		String result = processor.process(text, Locale.US);
		assertEquals("<red>[Intercept] Hello!", result);
	}

	@Test
	void shouldKeepOriginalWhenReferenceMissing() {
		String text = "<m:missing> Hello!";
		String result = processor.process(text, Locale.US);
		assertEquals("<m:missing> Hello!", result);
	}

	@Test
	void shouldHandleTextWithNoReferences() {
		String text = "Just plain text";
		String result = processor.process(text, Locale.US);
		assertEquals("Just plain text", result);
	}

	@Test
	void shouldResolveReferenceWithDots() {
		registry.register("common.prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[Common]"));

		String text = "<m:common.prefix> Message";
		String result = processor.process(text, Locale.US);
		assertEquals("[Common] Message", result);
	}

	@Test
	void shouldHandleReferenceChain() {
		registry.register("a", new DefaultMessageEntry(MessageType.TEMPLATE, "A"));
		registry.register("b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>B"));
		registry.register("c", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>C"));

		String text = "<m:c>";
		String result = processor.process(text, Locale.US);
		assertEquals("ABC", result);
	}

	@Test
	void shouldPreventInfiniteLoop() {
		registry.register("a", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>"));
		registry.register("b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>"));

		String text = "<m:a>";
		String result = processor.process(text, Locale.US);
		// Should stop after max depth and keep unresolved
		assertTrue(result.contains("<m:"));
	}
}