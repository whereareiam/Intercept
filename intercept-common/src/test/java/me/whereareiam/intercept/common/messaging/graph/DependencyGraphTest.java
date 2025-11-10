package me.whereareiam.intercept.common.messaging.graph;

import me.whereareiam.intercept.common.messaging.DefaultMessageEntry;
import me.whereareiam.intercept.common.messaging.DependencyGraph;
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
		Map<String, DefaultMessageEntry> entries = Map.of(
				"msg", new DefaultMessageEntry(MessageType.MESSAGE, "<m:prefix> Hello")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("msg");
		assertTrue(deps.contains("prefix"));
	}

	@Test
	void shouldExtractTemplateDependencies() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"msg", new DefaultMessageEntry(MessageType.MESSAGE, "<tpl:error-format message='Error'>")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("msg");
		assertTrue(deps.contains("error-format"));
	}

	@Test
	void shouldExtractMultipleDependencies() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"msg", new DefaultMessageEntry(MessageType.MESSAGE,
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
		Map<String, DefaultMessageEntry> entries = Map.of(
				"a", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
		assertTrue(graph.hasCircularDependency("b"));
	}

	@Test
	void shouldDetectIndirectCircularDependency() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"a", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:c>"),
				"c", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
		assertTrue(graph.hasCircularDependency("b"));
		assertTrue(graph.hasCircularDependency("c"));
	}

	@Test
	void shouldDetectSelfReference() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"a", new DefaultMessageEntry(MessageType.TEMPLATE, "Text <m:a> more")
		);

		graph.build(entries);

		assertTrue(graph.hasCircularDependency("a"));
	}

	@Test
	void shouldNotDetectCircularForAcyclicGraph() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"a", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>"),
				"b", new DefaultMessageEntry(MessageType.TEMPLATE, "Text"),
				"c", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>")
		);

		graph.build(entries);

		assertFalse(graph.hasCircularDependency("a"));
		assertFalse(graph.hasCircularDependency("b"));
		assertFalse(graph.hasCircularDependency("c"));
	}

	@Test
	void shouldCalculateTopologicalOrder() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"a", new DefaultMessageEntry(MessageType.TEMPLATE, "A"),
				"b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>B"),
				"c", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>C")
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
		Map<String, DefaultMessageEntry> entries = Map.of(
				"simple", new DefaultMessageEntry(MessageType.MESSAGE, "No dependencies")
		);

		graph.build(entries);

		Set<String> deps = graph.getDependencies("simple");
		assertTrue(deps.isEmpty());
	}

	@Test
	void shouldHandleComplexDependencyChain() {
		Map<String, DefaultMessageEntry> entries = Map.of(
				"color", new DefaultMessageEntry(MessageType.TEMPLATE, "<red>"),
				"prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:color>[App]"),
				"format", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:prefix> <p:msg>"),
				"message", new DefaultMessageEntry(MessageType.MESSAGE, "<tpl:format msg='Hello'>")
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