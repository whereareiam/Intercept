package me.whereareiam.intercept.common.messaging.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheStrategyTest {
	private CacheStrategy strategy;

	@BeforeEach
	void setUp() {
		strategy = new CacheStrategy();
	}

	@Test
	void shouldClassifyFullyStaticMessage() {
		String text = "This is plain text with no tags";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.STATIC, level);
	}

	@Test
	void shouldClassifyMessageWithOnlyReferences() {
		String text = "<m:common.prefix> This has only references <m:common.suffix>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.STATIC, level); // References are static
	}

	@Test
	void shouldClassifyMessageWithTemplatesOnly() {
		String text = "<tpl:error-format message='Static text'>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.STATIC, level); // Static template params
	}

	@Test
	void shouldClassifyMessageWithPlaceholder() {
		String text = "Hello, <p:player>!";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.DYNAMIC, level); // Has runtime placeholder
	}

	@Test
	void shouldClassifyMessageWithStaticConditional() {
		String text = "<if enabled==true>Enabled<else>Disabled</if>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.SEMI_STATIC, level); // Condition but no dynamic placeholders
	}

	@Test
	void shouldClassifyMessageWithDynamicConditional() {
		String text = "<if online==true>Player <p:name> is online</if>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.DYNAMIC, level); // Has placeholder in condition
	}

	@Test
	void shouldClassifyComplexStatic() {
		String text = "<m:prefix> <tpl:error-format message='Error'> <m:suffix>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.STATIC, level);
	}

	@Test
	void shouldClassifyComplexSemiStatic() {
		String text = "<m:prefix> <if rank==admin>Admin<else>User</if>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.SEMI_STATIC, level);
	}

	@Test
	void shouldClassifyComplexDynamic() {
		String text = "<m:prefix> <tpl:player-name name='<p:player>'> joined";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.DYNAMIC, level);
	}

	@Test
	void shouldDetectPlaceholderInTemplateParam() {
		String text = "<tpl:format msg='<p:details>'>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.DYNAMIC, level); // Template param has placeholder
	}

	@Test
	void shouldDetectPlaceholderInCondition() {
		String text = "<if <p:var>==true>Yes</if>";
		CacheLevel level = strategy.classify(text);
		assertEquals(CacheLevel.DYNAMIC, level);
	}
}