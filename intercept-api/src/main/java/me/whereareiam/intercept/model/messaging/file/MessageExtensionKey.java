package me.whereareiam.intercept.model.messaging.file;

import java.util.Objects;

public final class MessageExtensionKey<T extends MessageExtensionPayload> {
	private final String id;
	private final Class<T> type;

	public MessageExtensionKey(String id, Class<T> type) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Extension id cannot be null or blank");
		}

		this.id = id;
		this.type = Objects.requireNonNull(type, "type");
	}

	public String id() {
		return id;
	}

	public Class<T> type() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MessageExtensionKey<?> that)) return false;

		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
