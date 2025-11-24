package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DependencyGraphTest {
	private DependencyGraph graph;

	@BeforeEach
	void setUp() {
		graph = new DependencyGraph();
	}

	@Test
	void shouldExtractMessageReferenceDependencies() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"msg", new CompiledMessageEntry(MessageType.MESSAGE, "<m:prefix> Hello")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("msg");
		assertTrue(deps.contains("prefix"));
	}

	@Test
	void shouldExtractTemplateDependencies() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"msg", new CompiledMessageEntry(MessageType.MESSAGE, "<tpl:error-format message='Error'>")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("msg");
		assertTrue(deps.contains("error-format"));
	}

	@Test
	void shouldExtractMultipleDependencies() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"msg", new CompiledMessageEntry(MessageType.MESSAGE,
						"<m:prefix> <tpl:format msg='<m:suffix>'>")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("msg");
		assertEquals(3, deps.size());
		assertTrue(deps.contains("prefix"));
		assertTrue(deps.contains("format"));
		assertTrue(deps.contains("suffix"));
	}

	@Test
	void shouldDetectDirectCircularDependency() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"a", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:a>")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
		assertTrue(graph.hasCircularDependency("b"));
	}

	@Test
	void shouldDetectIndirectCircularDependency() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"a", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:c>"),
				"c", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:a>")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
		assertTrue(graph.hasCircularDependency("b"));
		assertTrue(graph.hasCircularDependency("c"));
	}

	@Test
	void shouldDetectSelfReference() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"a", new CompiledMessageEntry(MessageType.TEMPLATE, "Text <m:a> more")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
	}

	@Test
	void shouldNotDetectCircularForAcyclicGraph() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"a", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new CompiledMessageEntry(MessageType.TEMPLATE, "Text"),
				"c", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>")
		);

		graph.build(entries);

		assertFalse(graph.hasCircularDependency("a"));
		assertFalse(graph.hasCircularDependency("b"));
		assertFalse(graph.hasCircularDependency("c"));
	}

	@Test
	void shouldCalculateTopologicalOrder() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"a", new CompiledMessageEntry(MessageType.TEMPLATE, "A"),
				"b", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:a>B"),
				"c", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:b>C")
		);

		graph.build(entries);

		List<String> order = graph.getResolutionOrder();

		// 'a' should come before 'b', 'b' before 'c'
		int aIndex = order.indexOf("a");
		int bIndex = order.indexOf("b");
		int cIndex = order.indexOf("c");

		assertTrue(aIndex < bIndex);
		assertTrue(bIndex < cIndex);
	}

	@Test
	void shouldHandleNoDependencies() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"simple", new CompiledMessageEntry(MessageType.MESSAGE, "No dependencies")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("simple");
		assertTrue(deps.isEmpty());
	}

	@Test
	void shouldHandleComplexDependencyChain() {
		Map<String, CompiledMessageEntry> entries = Map.of(
				"color", new CompiledMessageEntry(MessageType.TEMPLATE, "<red>"),
				"prefix", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:color>[App]"),
				"format", new CompiledMessageEntry(MessageType.TEMPLATE, "<m:prefix> <p:msg>"),
				"message", new CompiledMessageEntry(MessageType.MESSAGE, "<tpl:format msg='Hello'>")
		);

		graph.build(entries);

		List<String> order = graph.getResolutionOrder();

		// color -> prefix -> format -> message
		int colorIdx = order.indexOf("color");
		int prefixIdx = order.indexOf("prefix");
		int formatIdx = order.indexOf("format");
		int messageIdx = order.indexOf("message");

		assertTrue(colorIdx < prefixIdx);
		assertTrue(prefixIdx < formatIdx);
		assertTrue(formatIdx < messageIdx);
	}
}