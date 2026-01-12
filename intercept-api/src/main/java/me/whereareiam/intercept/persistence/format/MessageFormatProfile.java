package me.whereareiam.intercept.persistence.format;

import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

/**
 * Strategy interface that defines how to interpret and write message entries for a format.
 */
public interface MessageFormatProfile {
	default boolean isFlattened() {
		return false;
	}

	default String buildPrefix(FormatContext context) {
		return "";
	}

	default MessageFileData.Entry parseFlattenedValue(MessageValue value, FormatContext context) {
		return null;
	}

	default boolean isEntryObject(ObjectNode node, FormatContext context) {
		return false;
	}

	default MessageFileData.Entry parseEntryObject(ObjectNode node, FormatContext context) {
		return null;
	}

	default MessageFileData.Entry parseScalarValue(Node value, FormatContext context) {
		return null;
	}

	default Node writeEntry(MessageFileData.Entry entry, FormatContext context) {
		return NullNode.instance();
	}
}
