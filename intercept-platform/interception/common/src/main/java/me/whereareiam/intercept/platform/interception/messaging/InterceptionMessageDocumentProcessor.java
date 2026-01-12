package me.whereareiam.intercept.platform.interception.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.registry.InterceptionRegistry;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class InterceptionMessageDocumentProcessor {
	private final InterceptionRegistry registry;

	private static final MessageExtensionKey<MapMessageExtensionPayload> INTERCEPTION_KEY = new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);

	public void process(String keyPrefix, MessageFileData document) {
		if (document == null || document.getEntries().isEmpty()) return;

		String normalizedPrefix = normalizePrefix(keyPrefix);
		parseEntries(normalizedPrefix, document.getEntries());
	}

	private void parseEntries(String prefix, Map<String, MessageFileData.Node> source) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, MessageFileData.Node> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			MessageFileData.Node value = rawEntry.getValue();
			if (value == null) continue;
			String fullKey;
			if (prefix == null || prefix.isEmpty()) {
				fullKey = key;
			} else if (prefix.endsWith(":")) {
				fullKey = prefix + key;
			} else {
				fullKey = prefix + "." + key;
			}

			if (value instanceof MessageFileData.Entry entryData) {
				MessageExtensions extensions = entryData.getExtensions();
				MapMessageExtensionPayload payload = extensions == null ? null : extensions.get(INTERCEPTION_KEY);
				parseInterception(fullKey, payload);
				continue;
			}

			if (value instanceof MessageFileData.Section section)
				parseEntries(fullKey, section.getEntries());
		}
	}

	private void parseInterception(String key, MapMessageExtensionPayload payload) {
		if (payload == null) return;

		Object rawPatterns = payload.data().get("patterns");
		if (!(rawPatterns instanceof List<?> list)) return;

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (Object raw : list) {
			CompiledRegexPattern pattern = compilePattern(key, raw);
			if (pattern != null) compiled.add(pattern);
		}

		if (!compiled.isEmpty())
			registry.register(key, compiled);
	}

	private CompiledRegexPattern compilePattern(String key, Object raw) {
		if (!(raw instanceof Map<?, ?> map)) return null;

		Object patternValue = map.get("pattern");
		if (!(patternValue instanceof String pattern) || pattern.isBlank()) return null;

		Map<String, String> placeholders = parsePlaceholders(map.get("placeholders"));
		int priority = parseInt(map.get("priority"));
		boolean replaceMatched = parseBoolean(map.get("replaceMatched"));

		try {
			return new CompiledRegexPattern(
					pattern,
					placeholders,
					priority,
					replaceMatched
			);
		} catch (Exception e) {
			Logger.warn("Failed to compile regex for key '%s': %s", key, e.getMessage());
			return null;
		}
	}

	private String normalizePrefix(String keyPrefix) {
		if (keyPrefix == null || keyPrefix.isBlank()) return "";
		return keyPrefix.endsWith(".") ? keyPrefix.substring(0, keyPrefix.length() - 1) : keyPrefix;
	}

	private Map<String, String> parsePlaceholders(Object raw) {
		if (!(raw instanceof Map<?, ?> map)) return null;
		Map<String, String> placeholders = new LinkedHashMap<>();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) continue;
			placeholders.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}

		return placeholders.isEmpty() ? null : placeholders;
	}

	private int parseInt(Object raw) {
		if (raw instanceof Number number) return number.intValue();
		if (raw instanceof String text) {
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException ignored) {
				return 0;
			}
		}

		return 0;
	}

	private boolean parseBoolean(Object raw) {
		if (raw instanceof Boolean value) return value;
		if (raw instanceof String text) return Boolean.parseBoolean(text);

		return false;
	}
}
