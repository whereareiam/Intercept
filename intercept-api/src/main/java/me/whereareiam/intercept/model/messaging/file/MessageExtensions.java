package me.whereareiam.intercept.model.messaging.file;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MessageExtensions {
	private final Map<String, MessageExtensionPayload> entries = new LinkedHashMap<>();

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public Map<String, MessageExtensionPayload> entries() {
		return Map.copyOf(entries);
	}

	public MessageExtensionPayload get(String id) {
		if (id == null) return null;
		return entries.get(id);
	}

	public <T extends MessageExtensionPayload> T get(MessageExtensionKey<T> key) {
		if (key == null) return null;
		MessageExtensionPayload payload = entries.get(key.id());
		if (!key.type().isInstance(payload)) return null;

		return key.type().cast(payload);
	}

	public <T extends MessageExtensionPayload> void put(MessageExtensionKey<T> key, T payload) {
		if (key == null) return;
		if (payload == null) {
			entries.remove(key.id());
			return;
		}

		entries.put(key.id(), payload);
	}
}
