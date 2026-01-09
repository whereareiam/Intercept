package me.whereareiam.intercept.platform.interception.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagParserTest {
	@Test
	void shouldExtractSimpleTag() {
		String text = "Welcome <lang key=\"welcome.message\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals(1, tags.size());
		assertEquals("welcome.message", tags.get(0).key());
		assertEquals(0, tags.get(0).placeholders().size());
	}

	@Test
	void shouldExtractTagWithPlaceholders() {
		String text = "<lang key=\"player.join\" name=\"Steve\" rank=\"Admin\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals(1, tags.size());
		assertEquals("player.join", tags.get(0).key());
		assertEquals(2, tags.get(0).placeholders().size());
		assertEquals("Steve", tags.get(0).placeholders().get(0).value());
		assertEquals("Admin", tags.get(0).placeholders().get(1).value());
	}

	@Test
	void shouldExtractMultipleTags() {
		String text = "<lang key=\"prefix\"> Welcome <lang key=\"player.join\" name=\"Steve\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals(2, tags.size());
		assertEquals("prefix", tags.get(0).key());
		assertEquals("player.join", tags.get(1).key());
	}

	@Test
	void shouldIgnoreTagsWithoutKeyAttribute() {
		String text = "<lang name=\"Steve\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertTrue(tags.isEmpty());
	}

	@Test
	void shouldExtractTagWithEmptyValue() {
		String text = "<lang key=\"message\" param=\"\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals(1, tags.size());
		assertEquals("", tags.get(0).placeholders().get(0).value());
	}

	@Test
	void shouldNotExtractTagsForDifferentTagName() {
		String text = "<lang key=\"message\"> <other key=\"test\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "translate");

		assertTrue(tags.isEmpty());
	}

	@Test
	void shouldExtractTagWithSpecialCharactersInValue() {
		String text = "<lang key=\"message\" text=\"Hello, World! (123)\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals("Hello, World! (123)", tags.get(0).placeholders().get(0).value());
	}

	@Test
	void shouldExtractTagWithSquareBrackets() {
		String text = "Welcome [l key=\"welcome.message\"]";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "[l]");

		assertEquals(1, tags.size());
		assertEquals("welcome.message", tags.get(0).key());
	}

	@Test
	void shouldExtractTagWithCurlyBraces() {
		String text = "Hello {tr key=\"player.join\" name=\"Steve\"}";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "{tr}");

		assertEquals(1, tags.size());
		assertEquals("player.join", tags.get(0).key());
		assertEquals("Steve", tags.get(0).placeholders().get(0).value());
	}

	@Test
	void shouldDetectTagInText() {
		assertTrue(TagParser.containsTag("Welcome <lang key=\"message\">", "<lang>"));
		assertFalse(TagParser.containsTag("Welcome to the server", "<lang>"));
	}

	@Test
	void shouldDetectDifferentTagFormats() {
		assertTrue(TagParser.containsTag("Welcome [l key=\"message\"]", "[l]"));
		assertTrue(TagParser.containsTag("Welcome {tr key=\"message\"}", "{tr}"));
		assertFalse(TagParser.containsTag("Welcome <lang key=\"message\">", "[l]"));
	}

	@Test
	void shouldExtractTagWithUnquotedValues() {
		String text = "<lang key=welcome.message>";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals("welcome.message", tags.get(0).key());
	}

	@Test
	void shouldExtractTagWithUnquotedPlaceholders() {
		String text = "<lang key=player.join name=Steve rank=Admin>";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals("player.join", tags.get(0).key());
		assertEquals("Steve", tags.get(0).placeholders().get(0).value());
		assertEquals("Admin", tags.get(0).placeholders().get(1).value());
	}

	@Test
	void shouldExtractTagWithMixedQuotedAndUnquotedValues() {
		String text = "<lang key=\"player.join\" name=Steve rank=\"Admin\">";

		List<TagParser.TagData> tags = TagParser.extractTags(text, "<lang>");

		assertEquals("player.join", tags.get(0).key());
		assertEquals("Steve", tags.get(0).placeholders().get(0).value());
		assertEquals("Admin", tags.get(0).placeholders().get(1).value());
	}


}
