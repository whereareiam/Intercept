package me.whereareiam.intercept.interception.common.marker;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkerFormatTest {
	@Test
	void theDefaultShapeFindsAMarkerWithQuotedAndBareValues() {
		Matcher matcher = MarkerFormat.of("<lang>").pattern()
				.matcher("Before <lang key=\"essentials.motd\" player=Alice note=\"a > b\"> after");

		assertTrue(matcher.find());
		Marker marker = Marker.parse(matcher.group(1));
		assertEquals("essentials.motd", marker.key());
		assertEquals(Map.of("player", "Alice", "note", "a > b"), marker.arguments());
	}

	@Test
	void anotherShapeFindsOnlyItsOwnMarkers() {
		MarkerFormat brackets = MarkerFormat.of("[t]");

		assertTrue(brackets.pattern().matcher("[t key=a.b]").find());
		assertFalse(brackets.pattern().matcher("<lang key=a.b>").find());
	}

	@Test
	void aMarkerWithoutAKeyIsNone() {
		assertNull(Marker.parse("player=Alice"));
	}

	@Test
	void aShapeNeedsThreeCharacters() {
		assertThrows(IllegalArgumentException.class, () -> MarkerFormat.of("<>"));
	}
}
