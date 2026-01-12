package me.whereareiam.intercept.common.persistence.format;

import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.persistence.format.MessageFormatProfile;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

import java.util.Map;

/**
 * Shared codec that parses and writes message files using a format profile.
 */
public final class TranslationTreeCodec {
	public MessageFileData parse(ObjectNode rawData, FormatContext context, MessageFormatProfile profile) {
		MessageFileData fileData = new MessageFileData();
		if (rawData == null || rawData.getValues().isEmpty() || context == null || profile == null) return fileData;

		if (profile.isFlattened()) {
			parseFlattened(fileData, rawData, context, profile);
			return fileData;
		}

		parseSection(fileData.getEntries(), rawData, context, profile);
		return fileData;
	}

	public ObjectNode write(MessageFileData data, FormatContext context, MessageFormatProfile profile) {
		ObjectNode output = new ObjectNode();
		if (data == null || data.getEntries().isEmpty() || context == null || profile == null) return output;
		writeSection(data.getEntries(), output, context, profile);
		return output;
	}

	private void parseFlattened(
			MessageFileData fileData,
			ObjectNode rawData,
			FormatContext context,
			MessageFormatProfile profile
	) {
		String prefix = profile.buildPrefix(context);
		Map<String, MessageValue> flattened = MessageFormatUtil.flattenToValues(rawData);
		for (Map.Entry<String, MessageValue> entry : flattened.entrySet()) {
			String key = entry.getKey();
			if (key == null) continue;
			MessageFileData.Entry fileEntry = profile.parseFlattenedValue(entry.getValue(), context);
			if (fileEntry == null) continue;
			String fullKey = MessageFormatUtil.applyPrefix(prefix, key);
			MessageFormatUtil.putEntry(fileData, fullKey, fileEntry);
		}
	}

	private void parseSection(
			Map<String, MessageFileData.Node> target,
			ObjectNode source,
			FormatContext context,
			MessageFormatProfile profile
	) {
		for (Map.Entry<String, Node> entry : source.getValues().entrySet()) {
			String key = entry.getKey();
			Node value = entry.getValue();
			if (key == null || value == null) continue;

			if (value instanceof ObjectNode objectNode) {
				if (profile.isEntryObject(objectNode, context)) {
					MessageFileData.Entry parsed = profile.parseEntryObject(objectNode, context);
					if (parsed != null) {
						target.put(key, parsed);
					}
				} else {
					MessageFileData.Section section = new MessageFileData.Section();
					parseSection(section.getEntries(), objectNode, context, profile);
					if (!section.getEntries().isEmpty()) {
						target.put(key, section);
					}
				}
				continue;
			}

			MessageFileData.Entry parsed = profile.parseScalarValue(value, context);
			if (parsed != null) {
				target.put(key, parsed);
			}
		}
	}

	private void writeSection(
			Map<String, MessageFileData.Node> source,
			ObjectNode output,
			FormatContext context,
			MessageFormatProfile profile
	) {
		for (Map.Entry<String, MessageFileData.Node> entry : source.entrySet()) {
			String key = entry.getKey();
			MessageFileData.Node node = entry.getValue();
			if (key == null || node == null) continue;

			if (node instanceof MessageFileData.Entry fileEntry) {
				Node raw = profile.writeEntry(fileEntry, context);
				if (raw != null && !(raw instanceof NullNode)) {
					output.getValues().put(key, raw);
				}
				continue;
			}

			if (node instanceof MessageFileData.Section section) {
				ObjectNode nested = new ObjectNode();
				writeSection(section.getEntries(), nested, context, profile);
				if (!nested.getValues().isEmpty()) {
					output.getValues().put(key, nested);
				}
			}
		}
	}
}
