package me.whereareiam.intercept.persistence.format;

import me.whereareiam.intercept.model.messaging.file.MessageExtensionPayload;

/**
 * Handler for platform-specific reserved keys.
 */
public interface ReservedKeyHandler {
	/**
	 * Reserved key identifier (e.g., "interception").
	 *
	 * @return reserved key
	 */
	String getKey();

	/**
	 * Parse raw value into a structured payload.
	 *
	 * @param rawValue raw value from config
	 * @return parsed payload
	 */
	MessageExtensionPayload parse(Object rawValue);

	/**
	 * Serialize a payload back into raw value.
	 *
	 * @param payload payload to serialize
	 * @return raw value for config
	 */
	Object serialize(MessageExtensionPayload payload);
}
