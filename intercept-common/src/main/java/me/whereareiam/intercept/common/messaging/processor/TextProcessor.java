package me.whereareiam.intercept.common.messaging.processor;

import java.util.List;

/**
 * Processes text from various formats (string or array) into a single string.
 */
public class TextProcessor {
	/**
	 * Process text from either a String or List&lt;String&gt; into a single string.
	 * Lists are joined with newlines.
	 *
	 * @param text the text object (String or List&lt;String&gt;)
	 * @return the processed text
	 * @throws IllegalArgumentException if text is null or invalid type
	 */
	public String process(Object text) {
		if (text == null) throw new IllegalArgumentException("Text cannot be null");

		if (text instanceof String)
			return (String) text;

		if (text instanceof List<?> list)
			return String.join("\n", list.stream()
					.map(Object::toString)
					.toList());

		throw new IllegalArgumentException("Text must be String or List<String>, got: " + text.getClass());
	}
}