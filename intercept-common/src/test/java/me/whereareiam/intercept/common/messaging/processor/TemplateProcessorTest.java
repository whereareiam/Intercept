package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TemplateProcessorTest {
	private DefaultMessageRegistry registry;
	private TemplateProcessor processor;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry(mock(Registry.class));
		processor = new TemplateProcessor(registry);
	}

	@Test
	void shouldApplyTemplateWithSingleParameter() {
		registry.register("error.format", new CompiledMessageEntry(MessageType.TEMPLATE, "✗ <p:message>"));

		String text = "<tpl:error.format message='Something went wrong'>";
		String result = processor.process(text, Locale.US);
		assertEquals("✗ Something went wrong", result); // Parameters are applied during template expansion
	}

	@Test
	void shouldApplyTemplateWithMultipleParameters() {
		registry.register("box", new CompiledMessageEntry(MessageType.TEMPLATE, "║ <p:title> - <p:message> ║"));

		String text = "<tpl:box title='Error' message='Failed'>";
		String result = processor.process(text, Locale.US);
		assertEquals("║ Error - Failed ║", result);
	}

	@Test
	void shouldKeepOriginalWhenTemplateMissing() {
		String text = "<tpl:missing param='value'>";
		String result = processor.process(text, Locale.US);
		assertEquals("<tpl:missing param='value'>", result);
	}

	@Test
	void shouldHandleTextWithNoTemplates() {
		String text = "Just plain text";
		String result = processor.process(text, Locale.US);
		assertEquals("Just plain text", result);
	}

	@Test
	void shouldHandleTemplateWithNoParameters() {
		registry.register("prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "[Intercept]"));

		String text = "<tpl:prefix>";
		String result = processor.process(text, Locale.US);
		assertEquals("[Intercept]", result);
	}

	@Test
	void shouldParseParameterValuesWithQuotes() {
		registry.register("test", new CompiledMessageEntry(MessageType.TEMPLATE, "<p:val>"));

		String text = "<tpl:test val='value with spaces'>";
		String result = processor.process(text, Locale.US);
		assertEquals("value with spaces", result);
	}

	@Test
	void shouldHandleMultiLineTemplate() {
		registry.register("box", new CompiledMessageEntry(MessageType.TEMPLATE, "╔═══╗\n║ <p:text> ║\n╚═══╝"));

		String text = "<tpl:box text='Hi'>";
		String result = processor.process(text, Locale.US);
		assertEquals("╔═══╗\n║ Hi ║\n╚═══╝", result);
	}

	@Test
	void shouldHandleMultipleTemplates() {
		registry.register("prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "[<p:name>]"));
		registry.register("suffix", new CompiledMessageEntry(MessageType.TEMPLATE, "(<p:note>)"));

		String text = "<tpl:prefix name='App'> Message <tpl:suffix note='v1'>";
		String result = processor.process(text, Locale.US);
		assertEquals("[App] Message (v1)", result);
	}

	@Test
	void shouldReturnTemplateTextAsIs() {
		// Templates with parameters replace them immediately
		registry.register("fmt", new CompiledMessageEntry(MessageType.TEMPLATE, "Value: <p:value>"));

		String text = "<tpl:fmt value='123'>";
		String result = processor.process(text, Locale.US);

		// Template processor applies parameters immediately
		assertEquals("Value: 123", result);
	}
}