package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.writer.ConfigWriter;
import me.whereareiam.intercept.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageExtensionCodec {
	private static final ConfigReader JSON_READER = Config.reader(Format.JSON);
	private static final ConfigWriter JSON_WRITER = Config.writer(Format.JSON);

	public static String encode(Map<String, Object> data) {
		if (data == null) return null;
		Path tempFile = null;

		try {
			tempFile = Files.createTempFile("intercept-extension-", ".json");
			JSON_WRITER.write(tempFile, toMutableMap(data));
			return Files.readString(tempFile);
		} catch (Exception e) {
			Logger.warn("Failed to encode extension payload: %s", e.getMessage());
			return null;
		} finally {
			deleteTempFile(tempFile);
		}
	}

	public static Map<String, Object> decode(String payload) {
		if (payload == null || payload.isBlank()) return Map.of();
		Path tempFile = null;

		try {
			tempFile = Files.createTempFile("intercept-extension-", ".json");
			Files.writeString(tempFile, payload);
			@SuppressWarnings("unchecked")
			Map<String, Object> decoded = JSON_READER.load(tempFile, Map.class);
			return decoded == null ? Map.of() : decoded;
		} catch (IOException e) {
			Logger.warn("Failed to decode extension payload: %s", e.getMessage());
			return Map.of();
		} finally {
			deleteTempFile(tempFile);
		}
	}

	private static void deleteTempFile(Path tempFile) {
		if (tempFile == null) return;
		try {
			Files.deleteIfExists(tempFile);
		} catch (IOException e) {
			Logger.warn("Failed to delete temp extension payload file: %s", e.getMessage());
		}
	}

	private static Map<String, Object> toMutableMap(Map<String, Object> data) {
		Map<String, Object> mutable = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			String key = entry.getKey();
			if (key == null) continue;
			mutable.put(key, toMutableValue(entry.getValue()));
		}

		return mutable;
	}

	private static Object toMutableValue(Object value) {
		if (value == null) return null;

		if (value instanceof Map<?, ?> map) {
			Map<String, Object> nested = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				Object key = entry.getKey();
				if (key == null) continue;
				nested.put(key.toString(), toMutableValue(entry.getValue()));
			}

			return nested;
		}

		if (value instanceof Iterable<?> iterable) {
			List<Object> list = new ArrayList<>();
			for (Object item : iterable) {
				list.add(toMutableValue(item));
			}
			return list;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			List<Object> list = new ArrayList<>(length);
			for (int i = 0; i < length; i++)
				list.add(toMutableValue(Array.get(value, i)));

			return list;
		}

		return value;
	}
}
