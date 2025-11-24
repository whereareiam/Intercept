package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Represents a single regex pattern entry from a message file.
 * This is the YAML/JSON representation before compilation.
 */
@Getter
@Setter
public class MessageDocumentRegex {
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

	/**
	 * When true, only the matched portion of the intercepted text should be replaced
	 * with the resolved message. When false (default), the whole intercepted message
	 * will be replaced.
	 */
	private boolean replaceMatched = false;
}
