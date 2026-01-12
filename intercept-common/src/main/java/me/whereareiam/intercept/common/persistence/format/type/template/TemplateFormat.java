package me.whereareiam.intercept.common.persistence.format.type.template;

import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.common.persistence.format.TranslationTreeCodec;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;

/**
 * TEMPLATE format: templates shared across locales.
 */
public class TemplateFormat implements MessageFormat {
	private static final TranslationTreeCodec CODEC = new TranslationTreeCodec();
	private static final TemplateFormatProfile PROFILE = new TemplateFormatProfile();

	@Override
	public String getId() {
		return "TEMPLATE";
	}

	@Override
	public MessageFileData parse(ObjectNode rawData, FormatContext context) {
		return CODEC.parse(rawData, context, PROFILE);
	}

	@Override
	public ObjectNode write(MessageFileData data, FormatContext context) {
		return CODEC.write(data, context, PROFILE);
	}

	@Override
	public boolean canRepresent(MessageFileData.Entry entry) {
		if (entry == null) return false;

		if (entry.getExtensions() != null && !entry.getExtensions().isEmpty())
			return false;

		if (entry.getLocales() != null && !entry.getLocales().isEmpty())
			return false;

		return entry.getText() != null;
	}
}

