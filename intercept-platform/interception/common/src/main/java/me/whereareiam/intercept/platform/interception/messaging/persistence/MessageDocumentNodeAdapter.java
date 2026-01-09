package me.whereareiam.intercept.platform.interception.messaging.persistence;

import me.whereareiam.configura.TypeAdapter;
import me.whereareiam.configura.node.ArrayNode;
import me.whereareiam.configura.node.BooleanNode;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.configura.node.NumberNode;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.configura.node.StringNode;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageDocumentNodeAdapter implements TypeAdapter<MessageDocument.Node> {
	private static final String TEXT_KEY = "text";
	private static final String LOCALES_KEY = "locales";
	private static final String INTERCEPTION_KEY = "interception";
	private static final String PATTERNS_KEY = "patterns";
	private static final String PATTERN_KEY = "pattern";
	private static final String PLACEHOLDERS_KEY = "placeholders";
	private static final String PRIORITY_KEY = "priority";
	private static final String REPLACE_MATCHED_KEY = "replaceMatched";

	@Override
	public MessageDocument.Node deserialize(String value) {
		if (value == null) return null;
		MessageDocument.Entry entry = new MessageDocument.Entry();
		entry.setText(MultiValue.of(value));
		return entry;
	}

	@Override
	public String serialize(MessageDocument.Node value) {
		Node node = serializeNode(value);
		return node != null ? node.asText() : null;
	}

	@Override
	public MessageDocument.Node deserializeNode(Node node) {
		return readNode(node);
	}

	@Override
	public Node serializeNode(MessageDocument.Node value) {
		return writeNode(value);
	}

	private MessageDocument.Node readNode(Node node) {
		if (node == null || node instanceof NullNode) return null;
		if (node instanceof StringNode stringNode) {
			MessageDocument.Entry entry = new MessageDocument.Entry();
			entry.setText(MultiValue.of(stringNode.getValue()));
			return entry;
		}
		if (node instanceof ArrayNode arrayNode) {
			MessageDocument.Entry entry = new MessageDocument.Entry();
			entry.setText(toMultiValue(arrayNode, TEXT_KEY));
			return entry;
		}
		if (node instanceof ObjectNode objectNode) {
			if (isEntryObject(objectNode)) {
				return readEntry(objectNode);
			}
			return readSection(objectNode);
		}
		if (node instanceof NumberNode || node instanceof BooleanNode) {
			throw new IllegalArgumentException("Namespace entry must be a string or array");
		}

		return null;
	}

	private boolean isEntryObject(ObjectNode objectNode) {
		Map<String, Node> values = objectNode.getValues();
		return values.containsKey(TEXT_KEY) || values.containsKey(LOCALES_KEY) || values.containsKey(INTERCEPTION_KEY);
	}

	private MessageDocument.Entry readEntry(ObjectNode objectNode) {
		MessageDocument.Entry entry = new MessageDocument.Entry();
		Map<String, Node> values = objectNode.getValues();

		Node textNode = values.get(TEXT_KEY);
		if (textNode != null && !(textNode instanceof NullNode)) {
			entry.setText(toMultiValue(textNode, TEXT_KEY));
		}

		Node localesNode = values.get(LOCALES_KEY);
		if (localesNode != null && !(localesNode instanceof NullNode)) {
			entry.setLocales(toLocales(localesNode));
		}

		Node interceptionNode = values.get(INTERCEPTION_KEY);
		if (interceptionNode != null && !(interceptionNode instanceof NullNode)) {
			entry.setInterception(toInterception(interceptionNode));
		}

		return entry;
	}

	private MessageDocument.Section readSection(ObjectNode objectNode) {
		MessageDocument.Section section = new MessageDocument.Section();
		for (Map.Entry<String, Node> entry : objectNode.getValues().entrySet()) {
			MessageDocument.Node child = readNode(entry.getValue());
			if (child != null) {
				section.putEntry(entry.getKey(), child);
			}
		}
		return section;
	}

	private MultiValue<String> toMultiValue(Node node, String field) {
		if (node == null) return null;
		if (node instanceof NullNode) {
			throw new IllegalArgumentException("Entry field '" + field + "' cannot be null");
		}
		if (node instanceof StringNode stringNode) {
			return MultiValue.of(stringNode.getValue());
		}
		if (node instanceof ArrayNode arrayNode) {
			List<String> values = new ArrayList<>();
			for (Node element : arrayNode.getValues()) {
				values.add(readScalarText(element, field));
			}
			return MultiValue.of(values);
		}
		throw new IllegalArgumentException("Entry field '" + field + "' must be a string or array");
	}

	private String readScalarText(Node node, String field) {
		if (node == null || node instanceof NullNode) {
			throw new IllegalArgumentException("Entry field '" + field + "' cannot contain null");
		}
		if (node instanceof StringNode stringNode) return stringNode.getValue();
		if (node instanceof NumberNode numberNode) return numberNode.asText();
		if (node instanceof BooleanNode booleanNode) return booleanNode.asText();
		throw new IllegalArgumentException("Entry field '" + field + "' must be a string or array");
	}

	private Map<String, MultiValue<String>> toLocales(Node node) {
		if (!(node instanceof ObjectNode objectNode)) {
			throw new IllegalArgumentException("Entry field '" + LOCALES_KEY + "' must be an object");
		}

		Map<String, MultiValue<String>> locales = new LinkedHashMap<>();
		for (Map.Entry<String, Node> entry : objectNode.getValues().entrySet()) {
			MultiValue<String> value = toMultiValue(entry.getValue(), LOCALES_KEY);
			if (value != null) {
				locales.put(entry.getKey(), value);
			}
		}
		return locales.isEmpty() ? null : locales;
	}

	private MessageDocument.Interception toInterception(Node node) {
		if (!(node instanceof ObjectNode objectNode)) {
			throw new IllegalArgumentException("Entry field '" + INTERCEPTION_KEY + "' must be an object");
		}

		Node patternsNode = objectNode.getValues().get(PATTERNS_KEY);
		if (!(patternsNode instanceof ArrayNode arrayNode)) {
			return null;
		}

		List<MessageDocument.Regex> patterns = new ArrayList<>();
		for (Node entry : arrayNode.getValues()) {
			if (entry instanceof ObjectNode patternNode) {
				patterns.add(readRegex(patternNode));
			}
		}

		MessageDocument.Interception interception = new MessageDocument.Interception();
		interception.setPatterns(patterns);
		return interception;
	}

	private MessageDocument.Regex readRegex(ObjectNode patternNode) {
		MessageDocument.Regex regex = new MessageDocument.Regex();
		Map<String, Node> values = patternNode.getValues();

		regex.setPattern(readString(values.get(PATTERN_KEY)));
		regex.setPriority(readInt(values.get(PRIORITY_KEY), 0));
		regex.setReplaceMatched(readBoolean(values.get(REPLACE_MATCHED_KEY), false));

		Map<String, String> placeholders = readPlaceholders(values.get(PLACEHOLDERS_KEY));
		if (placeholders != null && !placeholders.isEmpty()) {
			regex.setPlaceholders(placeholders);
		}
		return regex;
	}

	private Map<String, String> readPlaceholders(Node node) {
		if (!(node instanceof ObjectNode objectNode)) return null;
		Map<String, String> placeholders = new LinkedHashMap<>();
		for (Map.Entry<String, Node> entry : objectNode.getValues().entrySet()) {
			String value = readString(entry.getValue());
			if (value != null) {
				placeholders.put(entry.getKey(), value);
			}
		}
		return placeholders.isEmpty() ? null : placeholders;
	}

	private String readString(Node node) {
		if (node == null || node instanceof NullNode) return null;
		if (node instanceof StringNode stringNode) return stringNode.getValue();
		if (node instanceof NumberNode numberNode) return numberNode.asText();
		if (node instanceof BooleanNode booleanNode) return booleanNode.asText();
		return null;
	}

	private int readInt(Node node, int fallback) {
		if (node instanceof NumberNode numberNode && numberNode.getValue() != null) {
			return numberNode.getValue().intValue();
		}
		if (node instanceof StringNode stringNode) {
			try {
				return Integer.parseInt(stringNode.getValue());
			} catch (NumberFormatException ignored) {
				return fallback;
			}
		}
		return fallback;
	}

	private boolean readBoolean(Node node, boolean fallback) {
		if (node instanceof BooleanNode booleanNode) return booleanNode.getValue();
		if (node instanceof StringNode stringNode) return Boolean.parseBoolean(stringNode.getValue());
		return fallback;
	}

	private Node writeNode(MessageDocument.Node value) {
		if (value == null) return NullNode.instance();
		if (value instanceof MessageDocument.Entry entry) return writeEntry(entry);
		if (value instanceof MessageDocument.Section section) return writeSection(section);
		return NullNode.instance();
	}

	private Node writeSection(MessageDocument.Section section) {
		Map<String, Node> values = new LinkedHashMap<>();
		for (Map.Entry<String, MessageDocument.Node> entry : section.getEntries().entrySet()) {
			Node child = writeNode(entry.getValue());
			if (child != null && !(child instanceof NullNode)) {
				values.put(entry.getKey(), child);
			}
		}
		return new ObjectNode(values);
	}

	private Node writeEntry(MessageDocument.Entry entry) {
		Map<String, Node> values = new LinkedHashMap<>();

		Node textNode = toNode(entry.getText());
		if (textNode != null) {
			values.put(TEXT_KEY, textNode);
		}

		Node localesNode = toLocalesNode(entry.getLocales());
		if (localesNode != null) {
			values.put(LOCALES_KEY, localesNode);
		}

		Node interceptionNode = toInterceptionNode(entry.getInterception());
		if (interceptionNode != null) {
			values.put(INTERCEPTION_KEY, interceptionNode);
		}

		return new ObjectNode(values);
	}

	private Node toNode(MultiValue<String> value) {
		if (value == null || value.asList().isEmpty()) return null;

		List<String> list = value.asList();
		if (value.isSinglePreferred() && list.size() == 1) {
			return new StringNode(list.get(0));
		}

		List<Node> nodes = new ArrayList<>();
		for (String entry : list) {
			nodes.add(entry != null ? new StringNode(entry) : NullNode.instance());
		}
		return new ArrayNode(nodes);
	}

	private Node toLocalesNode(Map<String, MultiValue<String>> locales) {
		if (locales == null || locales.isEmpty()) return null;
		Map<String, Node> values = new LinkedHashMap<>();
		for (Map.Entry<String, MultiValue<String>> entry : locales.entrySet()) {
			Node node = toNode(entry.getValue());
			if (node != null) {
				values.put(entry.getKey(), node);
			}
		}
		return values.isEmpty() ? null : new ObjectNode(values);
	}

	private Node toInterceptionNode(MessageDocument.Interception interception) {
		if (interception == null || interception.getPatterns() == null || interception.getPatterns().isEmpty()) {
			return null;
		}

		List<Node> patterns = new ArrayList<>();
		for (MessageDocument.Regex regex : interception.getPatterns()) {
			patterns.add(toRegexNode(regex));
		}

		Map<String, Node> values = new LinkedHashMap<>();
		values.put(PATTERNS_KEY, new ArrayNode(patterns));
		return new ObjectNode(values);
	}

	private Node toRegexNode(MessageDocument.Regex regex) {
		Map<String, Node> values = new LinkedHashMap<>();

		if (regex.getPattern() != null) {
			values.put(PATTERN_KEY, new StringNode(regex.getPattern()));
		}

		Map<String, String> placeholders = regex.getPlaceholders();
		if (placeholders != null && !placeholders.isEmpty()) {
			Map<String, Node> placeholderNodes = new LinkedHashMap<>();
			for (Map.Entry<String, String> entry : placeholders.entrySet()) {
				if (entry.getValue() != null) {
					placeholderNodes.put(entry.getKey(), new StringNode(entry.getValue()));
				}
			}
			if (!placeholderNodes.isEmpty()) {
				values.put(PLACEHOLDERS_KEY, new ObjectNode(placeholderNodes));
			}
		}

		values.put(PRIORITY_KEY, new NumberNode(regex.getPriority()));
		values.put(REPLACE_MATCHED_KEY, new BooleanNode(regex.isReplaceMatched()));

		return new ObjectNode(values);
	}
}
