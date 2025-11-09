package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.messaging.processor.conditional.ConditionalProcessor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionalProcessorTest {
	private final ConditionalProcessor processor = new ConditionalProcessor();

	@Test
	void shouldProcessIfWithTrueCondition() {
		String text = "Status: <if online==true>Online</if>";
		String result = processor.process(text, Map.of("online", true));
		assertEquals("Status: Online", result);
	}

	@Test
	void shouldProcessIfElseWithTrueCondition() {
		String text = "Status: <if online==true>Online<else>Offline</if>";
		String result = processor.process(text, Map.of("online", true));
		assertEquals("Status: Online", result);
	}

	@Test
	void shouldProcessIfElseWithFalseCondition() {
		String text = "Status: <if online==true>Online<else>Offline</if>";
		String result = processor.process(text, Map.of("online", false));
		assertEquals("Status: Offline", result);
	}

	@Test
	void shouldRemoveBlockWhenConditionFalseWithoutElse() {
		String text = "<if vip==true>[VIP] </if>Welcome";
		String result = processor.process(text, Map.of("vip", false));
		assertEquals("Welcome", result);
	}

	@Test
	void shouldEvaluateEqualsOperator() {
		String text = "<if rank==admin>Admin</if>";
		String result = processor.process(text, Map.of("rank", "admin"));
		assertEquals("Admin", result);
	}

	@Test
	void shouldEvaluateNotEqualsOperator() {
		String text = "<if rank!=player>Special</if>";
		String result = processor.process(text, Map.of("rank", "admin"));
		assertEquals("Special", result);
	}

	@Test
	void shouldEvaluateGreaterThanOperator() {
		String text = "<if health>50>Good<else>Low</if>";
		String result = processor.process(text, Map.of("health", 75));
		assertEquals("Good", result);
	}

	@Test
	void shouldEvaluateGreaterOrEqualsOperator() {
		String text = "<if score>=100>Pass</if>";
		String result = processor.process(text, Map.of("score", 100));
		assertEquals("Pass", result);
	}

	@Test
	void shouldEvaluateLessThanOperator() {
		String text = "<if age<18>Minor<else>Adult</if>";
		String result = processor.process(text, Map.of("age", 15));
		assertEquals("Minor", result);
	}

	@Test
	void shouldEvaluateLessOrEqualsOperator() {
		String text = "<if count<=0>Empty</if>";
		String result = processor.process(text, Map.of("count", 0));
		assertEquals("Empty", result);
	}

	@Test
	void shouldEvaluateContainsOperator() {
		String text = "<if name~=Steve>Found</if>";
		String result = processor.process(text, Map.of("name", "Steve123"));
		assertEquals("Found", result);
	}

	@Test
	void shouldEvaluateStartsWithOperator() {
		String text = "<if name^=Mr>Formal</if>";
		String result = processor.process(text, Map.of("name", "Mr. Smith"));
		assertEquals("Formal", result);
	}

	@Test
	void shouldEvaluateEndsWithOperator() {
		String text = "<if file$=.txt>Text</if>";
		String result = processor.process(text, Map.of("file", "doc.txt"));
		assertEquals("Text", result);
	}

	@Test
	void shouldEvaluateNotEmptyOperator() {
		String text = "<if reason?>Reason: <p:reason></if>";
		String result = processor.process(text, Map.of("reason", "test"));
		assertEquals("Reason: <p:reason>", result);
	}

	@Test
	void shouldEvaluateEmptyOperator() {
		String text = "<if desc!?>No description</if>";
		String result = processor.process(text, Map.of("desc", ""));
		assertEquals("No description", result);
	}

	@Test
	void shouldHandleMissingPlaceholder() {
		String text = "<if missing==true>Yes<else>No</if>";
		String result = processor.process(text, Map.of());
		assertEquals("No", result);
	}

	@Test
	void shouldHandleNestedConditionals() {
		String text = "<if rank==admin>Admin<else><if rank==mod>Mod<else>Player</if></if>";
		String result = processor.process(text, Map.of("rank", "mod"));
		assertEquals("Mod", result);
	}

	@Test
	void shouldHandleMultipleConditionals() {
		String text = "<if a==1>A</if> <if b==2>B</if>";
		String result = processor.process(text, Map.of("a", 1, "b", 2));
		assertEquals("A B", result);
	}

	@Test
	void shouldHandleTextWithNoConditionals() {
		String text = "Just plain text";
		String result = processor.process(text, Map.of());
		assertEquals("Just plain text", result);
	}

	@Test
	void shouldHandleEmptyConditionBlock() {
		String text = "Start <if test==true></if> End";
		String result = processor.process(text, Map.of("test", true));
		assertEquals("Start  End", result);
	}

	@Test
	void shouldHandleBooleanValues() {
		String text = "<if flag==true>Yes</if>";
		String result = processor.process(text, Map.of("flag", true));
		assertEquals("Yes", result);
	}

	@Test
	void shouldHandleNumericComparison() {
		String text = "<if value>100>High</if>";
		String result = processor.process(text, Map.of("value", "150"));
		assertEquals("High", result);
	}
}