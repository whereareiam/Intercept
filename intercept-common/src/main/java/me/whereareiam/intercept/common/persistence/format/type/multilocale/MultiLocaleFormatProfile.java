package me.whereareiam.intercept.common.persistence.format.type.multilocale;

import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.persistence.format.MessageFormatProfile;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.persistence.format.ReservedKeyHandler;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Profile for MULTI_LOCALE format (entry maps with locale keys and extensions).
 */
public final class MultiLocaleFormatProfile implements MessageFormatProfile {
	private static final String TEXT_KEY = "text";
	private static final String LOCALES_KEY = "locales";
	private static final String RAW_KEY = "_raw";

	@Override
	public boolean isEntryObject(ObjectNode node, FormatContext context) {
		if (node == null || context == null) return false;
		Map<String, Node> values = node.getValues();
		if (values.containsKey(TEXT_KEY) || values.containsKey(LOCALES_KEY)) return true;
		Locale defaultLocale = context.getDefaultLocale();
		ReservedKeyRegistry registry = context.getReservedKeyRegistry();
		boolean hasLocaleKey = false;
		boolean hasNonLocale = false;
		for (String key : values.keySet()) {
			if (MessageFormatUtil.isLocaleKey(key, defaultLocale)) {
				hasLocaleKey = true;
				continue;
			}
			if (registry != null && registry.isReservedKey(key)) {
				continue;
			}
			hasNonLocale = true;
		}
		return hasLocaleKey && !hasNonLocale;
	}

	@Override
	public MessageFileData.Entry parseScalarValue(Node value, FormatContext context) {
		MessageValue text = MessageFormatUtil.toMessageValue(value);
		if (text == null) return null;
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(text);
		return entry;
	}

	@Override
	public MessageFileData.Entry parseEntryObject(ObjectNode mapValue, FormatContext context) {
		if (mapValue == null || context == null) return null;
		MessageFileData.Entry entry = new MessageFileData.Entry();
		Locale defaultLocale = context.getDefaultLocale();
		Map<String, Node> values = mapValue.getValues();

		MessageValue text = MessageFormatUtil.toMessageValue(values.get(TEXT_KEY));
		if (text != null) {
			entry.setText(text);
		}

		Map<String, MessageValue> locales = new LinkedHashMap<>();
		Node localesValue = values.get(LOCALES_KEY);
		if (localesValue instanceof ObjectNode localesNode) {
			collectLocales(localesNode, locales, defaultLocale);
		}

		for (Map.Entry<String, Node> field : values.entrySet()) {
			String key = field.getKey();
			if (key == null) continue;
			if (TEXT_KEY.equals(key) || LOCALES_KEY.equals(key)) continue;
			if (MessageFormatUtil.isLocaleKey(key, defaultLocale)) {
				MessageValue value = MessageFormatUtil.toMessageValue(field.getValue());
				String localeKey = normalizeLocaleKey(key, defaultLocale);
				if (value != null && localeKey != null) {
					locales.put(localeKey, value);
				}
			}
		}

		if (!locales.isEmpty()) {
			entry.setLocales(locales);
		}

		MessageExtensions extensions = parseExtensions(mapValue, context);
		if (extensions != null && !extensions.isEmpty()) {
			entry.setExtensions(extensions);
		}

		return entry;
	}

	@Override
	public Node writeEntry(MessageFileData.Entry entry, FormatContext context) {
		if (entry == null) return NullNode.instance();
		ObjectNode entryMap = new ObjectNode();

		MessageValue text = entry.getText();
		Node rawText = MessageFormatUtil.toNodeValue(text);
		if (rawText != null && !(rawText instanceof NullNode)) {
			entryMap.getValues().put(TEXT_KEY, rawText);
		}

		Map<String, MessageValue> locales = entry.getLocales();
		if (locales != null && !locales.isEmpty()) {
			for (Map.Entry<String, MessageValue> localeEntry : locales.entrySet()) {
				String localeKey = localeEntry.getKey();
				Node rawLocale = MessageFormatUtil.toNodeValue(localeEntry.getValue());
				if (localeKey != null && rawLocale != null && !(rawLocale instanceof NullNode)) {
					entryMap.getValues().put(localeKey, rawLocale);
				}
			}
		}

		MessageExtensions extensions = entry.getExtensions();
		if (extensions != null && !extensions.isEmpty()) {
			for (MessageExtensionPayload payload : extensions.entries().values()) {
				String extKey = payload.id();
				Node raw = serializePayload(payload, context);
				if (extKey != null && raw != null && !(raw instanceof NullNode)) {
					entryMap.getValues().put(extKey, raw);
				}
			}
		}

		return entryMap.getValues().isEmpty() ? NullNode.instance() : entryMap;
	}

	private void collectLocales(ObjectNode source, Map<String, MessageValue> target, Locale defaultLocale) {
		for (Map.Entry<String, Node> entry : source.getValues().entrySet()) {
			String key = entry.getKey();
			if (!MessageFormatUtil.isLocaleKey(key, defaultLocale)) continue;
			MessageValue value = MessageFormatUtil.toMessageValue(entry.getValue());
			String localeKey = normalizeLocaleKey(key, defaultLocale);
			if (value != null && localeKey != null) {
				target.put(localeKey, value);
			}
		}
	}

	private String normalizeLocaleKey(String key, Locale defaultLocale) {
		if (key == null || key.isBlank()) return null;
		if ("default".equalsIgnoreCase(key)) return "default";

		Locale parsed = MessageFormatUtil.parseLocaleToken(key, defaultLocale);
		if (parsed == null) return null;

		return parsed.getCountry().isEmpty() && parsed.getVariant().isEmpty()
				? parsed.getLanguage()
				: parsed.toString();
	}

	private MessageExtensions parseExtensions(ObjectNode mapValue, FormatContext context) {
		ReservedKeyRegistry registry = context.getReservedKeyRegistry();
		MessageExtensions extensions = new MessageExtensions();
		Locale defaultLocale = context.getDefaultLocale();

		for (Map.Entry<String, Node> entry : mapValue.getValues().entrySet()) {
			String key = entry.getKey();
			if (key == null) continue;
			if (TEXT_KEY.equals(key) || LOCALES_KEY.equals(key)) continue;
			if (MessageFormatUtil.isLocaleKey(key, defaultLocale)) continue;

			Node raw = entry.getValue();
			if (raw == null || raw instanceof NullNode) continue;
			Object rawValue = MessageFormatUtil.toRawObject(raw);

			MessageExtensionPayload payload = null;
			if (registry != null && registry.isReservedKey(key)) {
				Optional<ReservedKeyHandler> handler = registry.get(key);
				if (handler.isPresent()) {
					payload = handler.get().parse(rawValue);
				} else {
					payload = toRawPayload(key, rawValue);
				}
			} else if (isEntryObject(mapValue, context)) {
				payload = toRawPayload(key, rawValue);
			}

			if (payload != null) {
				putExtension(extensions, key, payload);
			}
		}

		return extensions.isEmpty() ? null : extensions;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void putExtension(MessageExtensions extensions, String key, MessageExtensionPayload payload) {
		MessageExtensionKey extensionKey = new MessageExtensionKey<>(key, (Class) payload.getClass());
		extensions.put(extensionKey, payload);
	}

	private MessageExtensionPayload toRawPayload(String key, Object raw) {
		if (key == null || key.isBlank() || raw == null) return null;
		if (raw instanceof Map<?, ?> map) {
			Map<String, Object> data = MessageFormatUtil.toStringMap(map);
			return new MapMessageExtensionPayload(key, data);
		}
		return new MapMessageExtensionPayload(key, Map.of(RAW_KEY, raw));
	}

	private Node serializePayload(MessageExtensionPayload payload, FormatContext context) {
		if (payload == null) return NullNode.instance();
		ReservedKeyRegistry registry = context == null ? null : context.getReservedKeyRegistry();
		if (registry != null) {
			Optional<ReservedKeyHandler> handler = registry.get(payload.id());
			if (handler.isPresent()) {
				return MessageFormatUtil.toNode(handler.get().serialize(payload));
			}
		}

		if (payload instanceof MapMessageExtensionPayload mapPayload) {
			Map<String, Object> data = mapPayload.data();
			if (data.containsKey(RAW_KEY)) {
				return MessageFormatUtil.toNode(data.get(RAW_KEY));
			}
			return MessageFormatUtil.toNode(data);
		}

		return NullNode.instance();
	}
}
