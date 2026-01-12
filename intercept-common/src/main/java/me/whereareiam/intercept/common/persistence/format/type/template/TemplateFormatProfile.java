package me.whereareiam.intercept.common.persistence.format.type.template;

import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.intercept.persistence.format.MessageFormatProfile;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

/**
 * Profile for TEMPLATE format (flattened text-only entries).
 */
public final class TemplateFormatProfile implements MessageFormatProfile {
	@Override
	public boolean isFlattened() {
		return true;
	}

	@Override
	public String buildPrefix(FormatContext context) {
		if (context == null) return "";
		return MessageFormatUtil.buildKeyPrefix(context.getRoot(), context.getFile());
	}

	@Override
	public MessageFileData.Entry parseFlattenedValue(MessageValue value, FormatContext context) {
		if (value == null) return null;
		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(value);
		return entry;
	}

	@Override
	public Node writeEntry(MessageFileData.Entry entry, FormatContext context) {
		if (entry == null) return NullNode.instance();

		if (entry.getExtensions() != null && !entry.getExtensions().isEmpty())
			return NullNode.instance();

		if (entry.getLocales() != null && !entry.getLocales().isEmpty())
			return NullNode.instance();

		MessageValue text = entry.getText();
		if (text == null) return NullNode.instance();

		return MessageFormatUtil.toNodeValue(text);
	}
}
