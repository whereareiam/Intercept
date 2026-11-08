package me.whereareiam.intercept.interception.common.rule;

import me.whereareiam.intercept.interception.Surface;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiled rule: a message whose plain text matches the pattern becomes the rule's key.
 *
 * @param id        the rule's name, prefixed by its file: {@code essentials/no-permission}
 * @param pattern   the pattern
 * @param key       the key to render
 * @param arguments argument names, each taken from a group number or a group name
 * @param surfaces  where the rule applies
 * @param priority  rules with a higher priority are tried first
 * @param whole     whether the rendered key replaces the whole message instead of the matched part
 */
public record Rule(
		@NotNull String id,
		@NotNull Pattern pattern,
		@NotNull String key,
		@NotNull Map<String, String> arguments,
		@NotNull Set<Surface> surfaces,
		int priority,
		boolean whole
) {
	/**
	 * Reads the arguments of one match.
	 *
	 * @param matcher a matcher of this rule's pattern that found a match
	 * @return the named arguments; a group that matched nothing is empty text
	 */
	public @NotNull Map<String, String> arguments(@NotNull Matcher matcher) {
		Map<String, String> values = new LinkedHashMap<>();
		arguments.forEach((name, group) -> {
			String value = group.chars().allMatch(Character::isDigit)
					? matcher.group(Integer.parseInt(group))
					: matcher.group(group);
			values.put(name, value == null ? "" : value);
		});

		return values;
	}
}
