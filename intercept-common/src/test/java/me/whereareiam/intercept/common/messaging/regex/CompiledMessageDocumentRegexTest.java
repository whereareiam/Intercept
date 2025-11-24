package me.whereareiam.intercept.common.messaging.regex;

import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CompiledMessageDocumentRegexTest {
	@Test
	void shouldMatchSimplePattern() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Hello (\\w+)",
				Map.of("name", "$1"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Hello World");

		assertTrue(result.isPresent());
		assertEquals("World", result.get().placeholders().get("name"));
	}

	@Test
	void shouldMatchMultipleCaptureGroups() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Player (\\w+) was banned by (\\w+)",
				Map.of("player", "$1", "moderator", "$2"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Player Steve was banned by Admin");

		assertTrue(result.isPresent());
		assertEquals("Steve", result.get().placeholders().get("player"));
		assertEquals("Admin", result.get().placeholders().get("moderator"));
	}

	@Test
	void shouldReturnEmptyWhenNoMatch() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Hello (\\w+)",
				Map.of("name", "$1"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Goodbye World");

		assertFalse(result.isPresent());
	}

	@Test
	void shouldHandleEmptyPlaceholders() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Test (\\w+)",
				null,
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Test Data");

		assertTrue(result.isPresent());
		assertTrue(result.get().placeholders().isEmpty());
	}

	@Test
	void shouldExtractLiteralPrefix() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"You don't have permission: (\\w+)",
				Map.of("permission", "$1"),
				0,
				false
		);

		String prefix = pattern.getLiteralPrefix();

		assertEquals("You don't have permission: ", prefix);
	}

	@Test
	void shouldExtractLiteralPrefixWithCaret() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"^Player (\\w+)",
				Map.of("name", "$1"),
				0,
				false
		);

		String prefix = pattern.getLiteralPrefix();

		assertEquals("Player ", prefix);
	}

	@Test
	void shouldReturnNullWhenNoLiteralPrefix() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"(\\w+) joined",
				Map.of("name", "$1"),
				0,
				false
		);

		String prefix = pattern.getLiteralPrefix();

		assertNull(prefix);
	}

	@Test
	void shouldCheckIfTextHasLiteralPrefix() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Player (\\w+) joined",
				Map.of("name", "$1"),
				0,
				false
		);

		assertTrue(pattern.hasLiteralPrefix("Player Steve joined"));
		assertFalse(pattern.hasLiteralPrefix("Steve joined"));
	}

	@Test
	void shouldSkipMatchingWhenPrefixDoesNotMatch() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Player (\\w+) joined",
				Map.of("name", "$1"),
				0,
				false
		);

		// Should return empty without running full regex when prefix doesn't match
		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Steve joined");

		assertFalse(result.isPresent());
	}

	@Test
	void shouldHandleNullCaptureGroup() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Player (\\w+)(?: was (\\w+))?",
				Map.of("player", "$1", "action", "$2"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Player Steve");

		assertTrue(result.isPresent());
		assertEquals("Steve", result.get().placeholders().get("player"));
		assertNull(result.get().placeholders().get("action"));
	}

	@Test
	void shouldStorePriority() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"Test (\\w+)",
				Map.of("name", "$1"),
				100,
				false
		);

		assertEquals(100, pattern.getPriority());
	}

	@Test
	void shouldHandleCaseInsensitivePattern() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"(?i)player (\\w+)",
				Map.of("name", "$1"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("PLAYER Steve");

		assertTrue(result.isPresent());
		assertEquals("Steve", result.get().placeholders().get("name"));
	}

	@Test
	void shouldExposeMatchBounds() {
		CompiledRegexPattern pattern = new CompiledRegexPattern(
				"error (\\d+)",
				Map.of("code", "$1"),
				0,
				false
		);

		Optional<CompiledRegexPattern.MatchResult> result = pattern.match("Unknown error 404 occurred");

		assertTrue(result.isPresent());
		assertEquals("error 404", result.get().matchedText());
		assertEquals(8, result.get().start());
		assertEquals(17, result.get().end());
	}
}