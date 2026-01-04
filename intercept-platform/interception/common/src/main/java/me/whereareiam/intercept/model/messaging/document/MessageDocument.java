package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.annotation.Field;
import me.whereareiam.configura.type.MultiValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the parsed data from a message file.
 * This is what we get after parsing YAML/JSON with Configura.
 */
@Getter
public class MessageDocument {
	@Field(dynamic = true)
	private final Map<String, Node> entries = new LinkedHashMap<>();

	public void putEntry(String key, Node value) {
		if (key == null) return;
		if (value == null) {
			entries.remove(key);
			return;
		}
		entries.put(key, value);
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	/**
	 * Marker for message document nodes.
	 */
	public interface Node {
	}

	/**
	 * Represents a nested section of entries.
	 */
	@Getter
	public static class Section implements Node {
		private final Map<String, Node> entries = new LinkedHashMap<>();

		public void putEntry(String key, Node value) {
			if (key == null) return;
			if (value == null) {
				entries.remove(key);
				return;
			}
			entries.put(key, value);
		}
	}

	/**
	 * Represents a single message entry from a file.
	 */
	@Getter
	@Setter
	public static class Entry implements Node {
		/**
		 * Single text (string or list).
		 * Used for templates or non-localized messages.
		 */
		private MultiValue<String> text;

		/**
		 * Locale translations grouped under "locales".
		 * Keys are locale strings or "default".
		 */
		private Map<String, MultiValue<String>> locales;

		/**
		 * Interception rules for this entry.
		 */
		private Interception interception;

		public void setLocales(Map<String, MultiValue<String>> locales) {
			this.locales = locales == null || locales.isEmpty() ? null : locales;
		}

		public void setInterception(Interception interception) {
			this.interception = (interception == null || interception.getPatterns() == null
					|| interception.getPatterns().isEmpty()) ? null : interception;
		}
	}

	/**
	 * Interception rules for a message key.
	 */
	@Getter
	@Setter
	public static class Interception {
		/**
		 * Regex patterns that can trigger this message.
		 */
		private List<Regex> patterns;

		public void setPatterns(List<Regex> patterns) {
			this.patterns = patterns == null || patterns.isEmpty() ? null : patterns;
		}
	}

	/**
	 * Represents a single regex pattern entry from a message file.
	 * This is the YAML/JSON representation before compilation.
	 */
	@Getter
	@Setter
	public static class Regex {
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

		public void setPlaceholders(Map<String, String> placeholders) {
			this.placeholders = placeholders == null || placeholders.isEmpty() ? null : placeholders;
		}
	}
}
