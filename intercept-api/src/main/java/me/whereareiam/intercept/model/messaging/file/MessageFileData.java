package me.whereareiam.intercept.model.messaging.file;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Namespace file data structure used for persistence and downloads.
 * This is not tied to a specific file format.
 */
@Getter
public class MessageFileData {
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
	 * Marker for message data nodes.
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
	 * Represents a single message entry.
	 */
	@Getter
	@Setter
	public static class Entry implements Node {
		/**
		 * Single text or list of lines.
		 */
		private MessageValue text;

		/**
		 * Locale translations grouped under locale keys.
		 */
		private Map<String, MessageValue> locales;

		/**
		 * Optional extensions for this entry.
		 */
		private MessageExtensions extensions;

		public void setLocales(Map<String, MessageValue> locales) {
			this.locales = locales == null || locales.isEmpty() ? null : locales;
		}

		public void setExtensions(MessageExtensions extensions) {
			this.extensions = extensions == null || extensions.isEmpty() ? null : extensions;
		}
	}
}
