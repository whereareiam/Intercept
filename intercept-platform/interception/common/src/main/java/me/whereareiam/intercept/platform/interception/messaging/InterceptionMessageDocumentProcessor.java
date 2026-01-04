package me.whereareiam.intercept.platform.interception.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.persistence.MessageDocumentProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentInterception;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class InterceptionMessageDocumentProcessor implements MessageDocumentProcessor {
	private static final String INTERCEPTION_KEY = "interception";
	private static final String PATTERNS_KEY = "patterns";
	private static final String TEXT_KEY = "text";
	private static final String LOCALES_KEY = "locales";

	private final InterceptionRegistry registry;

	@Inject
	public InterceptionMessageDocumentProcessor(InterceptionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void process(String keyPrefix, MessageDocument document) {
		if (document == null || document.isEmpty()) return;

		String normalizedPrefix = normalizePrefix(keyPrefix);
		parseEntries(normalizedPrefix, document.getEntries());
	}

	private void parseEntries(String prefix, Map<String, Object> source) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, Object> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			Object value = rawEntry.getValue();
			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

			if (value instanceof MessageDocumentEntry entryData) {
				parseInterception(fullKey, entryData.getInterception());
				continue;
			}

			if (value instanceof Map<?, ?> mapValue) {
				Map<String, Object> map = castMap(mapValue);
				if (isEntryMap(map)) {
					parseInterception(fullKey, map.get(INTERCEPTION_KEY));
					continue;
				}

				parseEntries(fullKey, map);
			}
		}
	}

	private boolean isEntryMap(Map<String, Object> map) {
		return map.containsKey(TEXT_KEY) || map.containsKey(LOCALES_KEY) || map.containsKey(INTERCEPTION_KEY);
	}

	private void parseInterception(String key, Object rawInterception) {
		if (!(rawInterception instanceof Map<?, ?> rawMap)) return;

		Map<String, Object> interceptionMap = castMap(rawMap);
		Object patternsRaw = interceptionMap.get(PATTERNS_KEY);
		if (!(patternsRaw instanceof List<?> list)) return;

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (Object entry : list) {
			if (!(entry instanceof Map<?, ?> patternMap)) continue;
			CompiledRegexPattern pattern = compilePattern(key, castMap(patternMap));
			if (pattern != null) compiled.add(pattern);
		}

		if (!compiled.isEmpty())
			registry.register(key, compiled);
	}

	private void parseInterception(String key, MessageDocumentInterception interception) {
		if (interception == null || interception.getPatterns() == null) return;

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (MessageDocumentRegex regex : interception.getPatterns()) {
			CompiledRegexPattern pattern = compilePattern(key, regex);
			if (pattern != null) compiled.add(pattern);
		}

		if (!compiled.isEmpty())
			registry.register(key, compiled);
	}

	private CompiledRegexPattern compilePattern(String key, Map<String, Object> patternMap) {
		String regex = readString(patternMap.get("pattern"));
		if (regex == null || regex.isBlank()) return null;

		Map<String, String> placeholders = castStringMap(patternMap.get("placeholders"));
		int priority = readInt(patternMap.get("priority"), 0);
		boolean replaceMatched = readBoolean(patternMap.get("replaceMatched"), false);

		try {
			return new CompiledRegexPattern(regex, placeholders, priority, replaceMatched);
		} catch (Exception e) {
			Logger.warn("Failed to compile regex for key '%s': %s", key, e.getMessage());
			return null;
		}
	}

	private CompiledRegexPattern compilePattern(String key, MessageDocumentRegex regex) {
		if (regex == null || regex.getPattern() == null || regex.getPattern().isBlank()) return null;

		try {
			return new CompiledRegexPattern(
					regex.getPattern(),
					regex.getPlaceholders(),
					regex.getPriority(),
					regex.isReplaceMatched()
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

	private Map<String, Object> castMap(Map<?, ?> raw) {
		Map<String, Object> casted = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			casted.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return casted;
	}

	private Map<String, String> castStringMap(Object raw) {
		if (!(raw instanceof Map<?, ?> mapValue)) return Map.of();
		Map<String, String> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
			result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
		return result;
	}

	private String readString(Object raw) {
		return raw != null ? String.valueOf(raw) : null;
	}

	private int readInt(Object raw, int fallback) {
		if (raw instanceof Number number) return number.intValue();
		if (raw instanceof String text) {
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException ignored) {
				return fallback;
			}
		}
		return fallback;
	}

	private boolean readBoolean(Object raw, boolean fallback) {
		if (raw instanceof Boolean value) return value;
		if (raw instanceof String text) return Boolean.parseBoolean(text);
		return fallback;
	}
}
