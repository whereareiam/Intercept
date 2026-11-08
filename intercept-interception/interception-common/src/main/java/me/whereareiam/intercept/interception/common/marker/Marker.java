package me.whereareiam.intercept.interception.common.marker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One marker's attributes: {@code key} names the message, every other attribute is a named argument.
 *
 * @param key       the message key
 * @param arguments the named arguments, as written
 */
public record Marker(@NotNull String key, @NotNull Map<String, String> arguments) {
	private static final Pattern ATTRIBUTE = Pattern.compile("([\\w.-]+)\\s*=\\s*(?:\"([^\"]*)\"|([^\\s\"]+))");

	/**
	 * Reads a marker's attributes.
	 *
	 * @param attributes the text between the marker's name and its closing, such as {@code key="a.b" player=Alice}
	 * @return the marker, or {@code null} when it has no key
	 */
	public static @Nullable Marker parse(@NotNull String attributes) {
		Map<String, String> values = new LinkedHashMap<>();
		Matcher matcher = ATTRIBUTE.matcher(attributes);
		while (matcher.find()) values.put(matcher.group(1), matcher.group(2) != null ? matcher.group(2) : matcher.group(3));

		String key = values.remove("key");
		return key == null || key.isBlank() ? null : new Marker(key, Map.copyOf(values));
	}
}
