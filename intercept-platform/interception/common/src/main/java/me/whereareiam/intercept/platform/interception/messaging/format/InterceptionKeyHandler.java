package me.whereareiam.intercept.platform.interception.messaging.format;

import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.ReservedKeyHandler;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionPayload;

import java.util.Map;

/**
 * Reserved key handler for interception regex payloads.
 */
public class InterceptionKeyHandler implements ReservedKeyHandler {
	@Override
	public String getKey() {
		return "interception";
	}

	@Override
	public MessageExtensionPayload parse(Object rawValue) {
		if (rawValue == null) return null;

		Map<String, Object> data = MessageFormatUtil.toStringMap(rawValue);
		if (data.isEmpty()) return null;

		return new MapMessageExtensionPayload(getKey(), data);
	}

	@Override
	public Object serialize(MessageExtensionPayload payload) {
		if (payload instanceof MapMessageExtensionPayload mapPayload)
			return mapPayload.data();

		return null;
	}
}
