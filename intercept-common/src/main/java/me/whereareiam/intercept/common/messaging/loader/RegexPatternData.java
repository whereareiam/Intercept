package me.whereareiam.intercept.common.messaging.loader;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Represents a single regex pattern entry from a message file.
 * This is the YAML/JSON representation before compilation.
 */
@Getter
@Setter
public class RegexPatternData {
	/**
	 * The regex pattern string to match against.
	 */
	private String pattern;

	/**
	 * Map of placeholder names to capture group references.
	 * e.g., {"permission": "$1", "player": "$2"}
	 */
	private Map<String, String> placeholders;

	/**
	 * Priority for pattern matching (higher = checked first).
	 * Default is 0.
	 */
	private int priority = 0;
}
