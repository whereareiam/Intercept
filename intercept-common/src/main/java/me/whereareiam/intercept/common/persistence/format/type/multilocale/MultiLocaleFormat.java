package me.whereareiam.intercept.common.persistence.format.type.multilocale;

import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.common.persistence.format.TranslationTreeCodec;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;

/**
 * MULTI_LOCALE format: all locales in one file.
 */
public class MultiLocaleFormat implements MessageFormat {
	private static final TranslationTreeCodec CODEC = new TranslationTreeCodec();
	private static final MultiLocaleFormatProfile PROFILE = new MultiLocaleFormatProfile();

	@Override
	public String getId() {
		return "MULTI_LOCALE";
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
		return entry != null;
	}
}
