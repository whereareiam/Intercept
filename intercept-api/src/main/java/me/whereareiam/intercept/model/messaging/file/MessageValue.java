package me.whereareiam.intercept.model.messaging.file;

import java.util.List;

/**
 * Represents a text value that can be a single line or multiple lines.
 */
@SuppressWarnings("unused")
public sealed interface MessageValue permits MessageValue.Text, MessageValue.Lines {
	static MessageValue text(String value) {
		return value == null ? null : new Text(value);
	}

	static MessageValue lines(List<String> values) {
		return values == null ? null : new Lines(List.copyOf(values));
	}

	default boolean isEmpty() {
		if (this instanceof Text text)
			return text.value() == null || text.value().isEmpty();

		if (this instanceof Lines lines)
			return lines.values() == null || lines.values().isEmpty();

		return true;
	}

	record Text(String value) implements MessageValue {
	}

	record Lines(List<String> values) implements MessageValue {
	}
}
