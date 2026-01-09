package me.whereareiam.intercept.model.messaging.file;

import java.util.Map;
import java.util.Objects;

public final class MapMessageExtensionPayload implements MessageExtensionPayload {
	private final String id;
	private final Map<String, Object> data;

	public MapMessageExtensionPayload(
			String id,
			Map<String, Object> data
	) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Extension id cannot be null or blank");
		}

		this.id = id;
		this.data = data == null ? Map.of() : Map.copyOf(data);
	}

	@Override
	public String id() {
		return id;
	}

	public Map<String, Object> data() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MapMessageExtensionPayload that)) return false;

		return id.equals(that.id)
				&& data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, data);
	}
}
