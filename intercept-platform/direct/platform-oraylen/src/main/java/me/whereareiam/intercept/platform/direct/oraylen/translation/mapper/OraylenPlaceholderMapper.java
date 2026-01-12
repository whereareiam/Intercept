package me.whereareiam.intercept.platform.direct.oraylen.translation.mapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.translation.mapper.PlaceholderMapper;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.kyori.adventure.text.Component;
import net.oraylen.api.translation.Placeholder;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class OraylenPlaceholderMapper implements PlaceholderMapper<Map<String, Placeholder>> {
	private final SerializerEngine serializerEngine;

	@Override
	public Map<String, Object> map(Map<String, Placeholder> placeholders) {
		if (placeholders == null || placeholders.isEmpty()) return Map.of();

		Map<String, Object> converted = new HashMap<>();
		for (Map.Entry<String, Placeholder> entry : placeholders.entrySet()) {
			String key = entry.getKey();
			if (key == null || key.isBlank()) continue;

			Placeholder placeholder = entry.getValue();
			switch (placeholder) {
				case null -> converted.put(key, "");
				case Placeholder.Text(String value) -> converted.put(key, value != null ? value : "");
				case Placeholder.Component(Component value) ->
						converted.put(key, value != null ? serializerEngine.serialize(value) : "");
			}
		}

		return converted;
	}
}
