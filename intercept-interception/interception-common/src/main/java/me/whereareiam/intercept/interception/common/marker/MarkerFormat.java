package me.whereareiam.intercept.interception.common.marker;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A marker's shape as configured: {@code <lang>} opens with {@code <}, is named {@code lang} and closes with
 * {@code >}, and matches {@code <lang key="essentials.motd" player="Alice">}.
 *
 * @param pattern finds one marker; its first group holds the attributes
 */
public record MarkerFormat(@NotNull Pattern pattern) {
	/**
	 * Reads a configured shape.
	 *
	 * @param shape the shape, at least three characters, such as {@code <lang>} or {@code [lang]}
	 * @return the format
	 * @throws IllegalArgumentException when the shape is too short
	 */
	public static @NotNull MarkerFormat of(@NotNull String shape) {
		if (shape.length() < 3) throw new IllegalArgumentException("A marker needs an opening, a name and a closing: " + shape);

		int last = shape.length() - 1;
		String name = Pattern.quote(shape.substring(1, last));
		char closing = shape.charAt(last);
		String body = "((?:\"[^\"]*\"|[^\"\\" + closing + "])*)";

		return new MarkerFormat(Pattern.compile(Pattern.quote(shape.substring(0, 1)) + name + "\\s+" + body + Pattern.quote(String.valueOf(closing))));
	}
}
