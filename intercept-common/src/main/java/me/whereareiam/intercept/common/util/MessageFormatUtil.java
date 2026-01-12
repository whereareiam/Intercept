package me.whereareiam.intercept.common.util;

import me.whereareiam.configura.node.ArrayNode;
import me.whereareiam.configura.node.BooleanNode;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.configura.node.NumberNode;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.configura.node.StringNode;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
import me.whereareiam.intercept.util.MessageKeyUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility helpers for message format parsing and writing.
 */
public final class MessageFormatUtil {
	public static MessageValue toMessageValue(Node raw) {
		if (raw == null || raw instanceof NullNode) return null;
		if (raw instanceof StringNode value)
			return MessageValue.text(value.getValue());

		if (raw instanceof NumberNode number)
			return MessageValue.text(String.valueOf(number.getValue()));

		if (raw instanceof BooleanNode bool)
			return MessageValue.text(String.valueOf(bool.getValue()));

		if (raw instanceof ArrayNode list) {
			List<String> values = new ArrayList<>();
			for (Node item : list.getValues()) {
				String rendered = renderNodeValue(item);
				if (rendered != null) values.add(rendered);
			}

			return values.isEmpty() ? null : MessageValue.lines(values);
		}

		return MessageValue.text(renderNodeValue(raw));
	}

	public static Node toNodeValue(MessageValue value) {
		if (value == null) return NullNode.instance();
		if (value instanceof MessageValue.Text text)
			return text.value() == null
					? NullNode.instance()
					: new StringNode(text.value());

		if (value instanceof MessageValue.Lines lines) {
			List<Node> nodes = new ArrayList<>();
			for (String entry : lines.values())
				nodes.add(entry == null
						? NullNode.instance()
						: new StringNode(entry));

			return new ArrayNode(nodes);
		}

		return NullNode.instance();
	}

	public static Object toRawObject(Node node) {
		if (node == null || node instanceof NullNode) return null;
		if (node instanceof StringNode value) return value.getValue();
		if (node instanceof NumberNode number) return number.getValue();
		if (node instanceof BooleanNode bool) return bool.getValue();
		if (node instanceof ArrayNode array) {
			List<Object> values = new ArrayList<>();
			for (Node item : array.getValues())
				values.add(toRawObject(item));

			return values;
		}

		if (node instanceof ObjectNode objectNode) {
			Map<String, Object> values = new LinkedHashMap<>();
			for (Map.Entry<String, Node> entry : objectNode.getValues().entrySet()) {
				if (entry.getKey() == null) continue;
				values.put(entry.getKey(), toRawObject(entry.getValue()));
			}

			return values;
		}

		return node.asText();
	}

	public static Node toNode(Object raw) {
		if (raw == null) return NullNode.instance();
		if (raw instanceof Node node) return node;
		if (raw instanceof String value) return new StringNode(value);
		if (raw instanceof Number value) return new NumberNode(value);
		if (raw instanceof Boolean value) return new BooleanNode(value);
		if (raw instanceof Map<?, ?> map) {
			Map<String, Node> values = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() == null) continue;
				values.put(String.valueOf(entry.getKey()), toNode(entry.getValue()));
			}

			return new ObjectNode(values);
		}

		if (raw instanceof List<?> list) {
			List<Node> values = new ArrayList<>();
			for (Object item : list)
				values.add(toNode(item));

			return new ArrayNode(values);
		}

		return new StringNode(String.valueOf(raw));
	}

	public static Map<String, Object> toStringMap(Object raw) {
		if (!(raw instanceof Map<?, ?> map)) return Map.of();
		Map<String, Object> converted = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() == null) continue;
			converted.put(String.valueOf(entry.getKey()), entry.getValue());
		}

		return converted;
	}

	public static Map<String, MessageValue> flattenToValues(ObjectNode content) {
		if (content == null || content.getValues().isEmpty()) return Map.of();
		Map<String, MessageValue> result = new LinkedHashMap<>();
		flattenMap("", content.getValues(), result);

		return result;
	}

	private static void flattenMap(String prefix, Map<String, Node> map, Map<String, MessageValue> result) {
		for (Map.Entry<String, Node> entry : map.entrySet()) {
			String key = entry.getKey();
			Node value = entry.getValue();
			if (key == null || value == null) continue;
			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

			if (value instanceof ObjectNode nested) {
				flattenMap(fullKey, nested.getValues(), result);
				continue;
			}

			MessageValue messageValue = toMessageValue(value);
			if (messageValue != null) {
				result.put(fullKey, messageValue);
			}
		}
	}

	private static String renderNodeValue(Node node) {
		if (node == null || node instanceof NullNode) return null;
		if (node instanceof StringNode stringNode) return stringNode.getValue();
		if (node instanceof NumberNode numberNode) return String.valueOf(numberNode.getValue());
		if (node instanceof BooleanNode boolNode) return String.valueOf(boolNode.getValue());

		Object raw = toRawObject(node);
		return raw == null
				? null
				: String.valueOf(raw);
	}

	public static Locale parseLocaleToken(String token, Locale defaultLocale) {
		return MessageKeyUtil.parseLocaleToken(token, defaultLocale);
	}

	public static boolean isLocaleKey(String token, Locale defaultLocale) {
		return MessageKeyUtil.isLocaleKey(token, defaultLocale);
	}

	public static LocaleMatch detectLocaleFromPath(Path root, Path file, Locale defaultLocale) {
		MessageKeyUtil.LocaleMatch match = MessageKeyUtil.detectLocaleFromPath(root, file, defaultLocale);
		return match == null ? null : new LocaleMatch(match.locale(), match.segmentIndex());
	}

	public static String buildKeyPrefix(Path root, Path file) {
		return MessageKeyUtil.buildKeyPrefix(root, file);
	}

	public static String buildKeyPrefixExcludingLocale(Path root, Path file, int localeSegmentIndex) {
		return MessageKeyUtil.buildKeyPrefixExcludingLocale(root, file, localeSegmentIndex);
	}

	public static String applyPrefix(String prefix, String key) {
		return MessageKeyUtil.applyPrefix(prefix, key);
	}

	public static void putEntry(MessageFileData fileData, String dotKey, MessageFileData.Entry entry) {
		MessageKeyUtil.putEntry(fileData, dotKey, entry);
	}

	public record LocaleMatch(Locale locale, int segmentIndex) {
	}
}
