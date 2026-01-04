package me.whereareiam.intercept.platform.interception.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class InterceptionMessageDocumentProcessor {
	private final InterceptionRegistry registry;

	public void process(String keyPrefix, MessageDocument document) {
		if (document == null || document.isEmpty()) return;

		String normalizedPrefix = normalizePrefix(keyPrefix);
		parseEntries(normalizedPrefix, document.getEntries());
	}

	private void parseEntries(String prefix, Map<String, MessageDocument.Node> source) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, MessageDocument.Node> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			MessageDocument.Node value = rawEntry.getValue();
			if (value == null) continue;
			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

			if (value instanceof MessageDocument.Entry entryData) {
				parseInterception(fullKey, entryData.getInterception());
				continue;
			}

			if (value instanceof MessageDocument.Section section) {
				parseEntries(fullKey, section.getEntries());
			}
		}
	}

	private void parseInterception(String key, MessageDocument.Interception interception) {
		if (interception == null || interception.getPatterns() == null) return;

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (MessageDocument.Regex regex : interception.getPatterns()) {
			CompiledRegexPattern pattern = compilePattern(key, regex);
			if (pattern != null) compiled.add(pattern);
		}

		if (!compiled.isEmpty())
			registry.register(key, compiled);
	}

	private CompiledRegexPattern compilePattern(String key, MessageDocument.Regex regex) {
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
}
