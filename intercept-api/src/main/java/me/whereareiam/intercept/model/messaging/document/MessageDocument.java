package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the parsed data from a message file.
 * This is what we get after parsing YAML/JSON with Configura.
 */
@Getter
public class MessageDocument {
	private final Map<String, Object> entries = new LinkedHashMap<>();

	public void putEntry(String key, Object value) {
		if (key == null) return;
		if (value == null) {
			entries.remove(key);
			return;
		}
		entries.put(key, value);
	}

	public Map<String, Object> getEntries() {
		return entries;
	}

	public void putEntries(Map<String, ?> values) {
		if (values == null || values.isEmpty()) return;
		for (Map.Entry<String, ?> entry : values.entrySet()) {
			putEntry(entry.getKey(), entry.getValue());
		}
	}

	public Map<String, Object> toMap() {
		return new LinkedHashMap<>(entries);
	}

	public static MessageDocument fromRawMap(Map<?, ?> raw) {
		MessageDocument document = new MessageDocument();
		if (raw == null || raw.isEmpty()) return document;
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			if (entry.getKey() == null) continue;
			document.putEntry(String.valueOf(entry.getKey()), entry.getValue());
		}
		return document;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
