package me.whereareiam.intercept.interception.common.rule;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.model.message.RuleFile;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Reads rule files. A rule in a local file replaces the shared rule with the same file and name.
 *
 * <pre>
 * # translations/rules/essentials.yml
 * no-permission:
 *   pattern: "You don't have permission: ([\\w.]+)"
 *   key: essentials.permission.missing
 *   arguments:
 *     permission: 1
 *   in: [chat, action-bar]
 *   priority: 10
 *   replace: match
 * </pre>
 */
@RequiredArgsConstructor
public final class RuleFileReader {
	private final @NotNull Configura configura;
	private final @NotNull Logger logger;

	/**
	 * Reads every rule of the files, skipping and logging the ones that cannot be used.
	 *
	 * @param files the files, lowest precedence first
	 * @return the rules, highest priority first
	 */
	public @NotNull List<Rule> read(@NotNull List<RuleFile> files) {
		Map<String, Rule> rules = new LinkedHashMap<>();
		for (RuleFile file : files) {
			JsonNode root;
			try {
				root = configura.readNode(file.getContent().getBytes(StandardCharsets.UTF_8));
			} catch (RuntimeException failure) {
				logger.warning("Cannot read rules/" + file.getPath() + ": " + failure.getMessage());
				continue;
			}
			if (root == null || !root.isObject()) continue;

			String prefix = stem(file.getPath());
			Iterator<Map.Entry<String, JsonNode>> entries = root.fields();
			while (entries.hasNext()) {
				Map.Entry<String, JsonNode> entry = entries.next();
				String id = prefix + "/" + entry.getKey();
				try {
					Definition definition = configura.mapper().treeToValue(entry.getValue(), Definition.class);
					rules.put(id, compile(id, definition));
				} catch (Exception failure) {
					logger.warning("Cannot use rule " + id + ": " + failure.getMessage());
				}
			}
		}

		List<Rule> sorted = new ArrayList<>(rules.values());
		sorted.sort(Comparator.comparingInt(Rule::priority).reversed());

		return List.copyOf(sorted);
	}

	private static Rule compile(String id, Definition definition) {
		if (definition.getPattern() == null || definition.getKey() == null)
			throw new IllegalArgumentException("a rule needs a pattern and a key");

		Pattern pattern;
		try {
			pattern = Pattern.compile(definition.getPattern());
		} catch (PatternSyntaxException failure) {
			throw new IllegalArgumentException("its pattern is invalid: " + failure.getDescription());
		}

		Set<Surface> surfaces = EnumSet.noneOf(Surface.class);
		if (definition.getIn().isEmpty()) surfaces.add(Surface.CHAT);
		for (String surface : definition.getIn())
			surfaces.add(Surface.valueOf(surface.toUpperCase(Locale.ROOT).replace('-', '_')));

		Map<String, String> arguments = new LinkedHashMap<>();
		definition.getArguments().forEach((name, group) -> arguments.put(name, String.valueOf(group)));

		boolean whole = "whole".equalsIgnoreCase(definition.getReplace());
		return new Rule(id, pattern, definition.getKey(), arguments, surfaces, definition.getPriority(), whole);
	}

	private static String stem(String path) {
		int dot = path.lastIndexOf('.');
		return dot < 0 ? path : path.substring(0, dot);
	}

	/**
	 * One rule as written in a file.
	 */
	@Getter
	@Setter
	public static class Definition {
		private String pattern;
		private String key;
		private Map<String, Object> arguments = new LinkedHashMap<>();
		private List<String> in = new ArrayList<>();
		private int priority;
		private String replace = "match";
	}
}
