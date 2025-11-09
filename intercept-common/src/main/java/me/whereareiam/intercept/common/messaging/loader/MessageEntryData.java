package me.whereareiam.intercept.common.messaging.loader;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.type.MessageType;

import java.util.List;
import java.util.Map;

/**
 * Represents a single message entry from a file.
 */
@Getter
@Setter
public class MessageEntryData {
	/**
	 * Optional entry-level type
	 * If not present, inherits from file-level type
	 * If file-level type also not present, auto-detect based on structure
	 */
	private MessageType type;

	/**
	 * Single-language text (String or List&lt;String&gt;)
	 * Used for templates or single-language messages
	 */
	private Object text;

	/**
	 * Multi-language translations (Map&lt;String, String or List&lt;String&gt;&gt;)
	 * Key = locale, Value = text (String or List&lt;String&gt;)
	 */
	private Map<String, Object> translations;

	/**
	 * Optional regex patterns that can trigger this message.
	 * When text matches any of these patterns, this message will be resolved.
	 */
	private List<RegexPatternData> regex;
}