package me.whereareiam.intercept.common.persistence.format.type.locale;

import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.common.persistence.format.TranslationTreeCodec;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.util.LocaleUtil;

import java.util.Locale;

/**
 * LOCALE format: one file per locale.
 */
public class LocaleFormat implements MessageFormat {
	private static final TranslationTreeCodec CODEC = new TranslationTreeCodec();

	@Override
	public String getId() {
		return "LOCALE";
	}

	@Override
	public MessageFileData parse(ObjectNode rawData, FormatContext context) {
		MessageFileData fileData = new MessageFileData();
		if (rawData == null || rawData.getValues().isEmpty() || context == null) return fileData;

		Locale defaultLocale = context.getDefaultLocale();
		MessageFormatUtil.LocaleMatch match = MessageFormatUtil.detectLocaleFromPath(
				context.getRoot(),
				context.getFile(),
				defaultLocale
		);
		if (match == null || match.locale() == null) return fileData;

		String prefix = MessageFormatUtil.buildKeyPrefixExcludingLocale(
				context.getRoot(),
				context.getFile(),
				match.segmentIndex()
		);
		String localeKey = LocaleUtil.formatLocale(match.locale());
		LocaleFormatProfile profile = new LocaleFormatProfile(localeKey, prefix);
		return CODEC.parse(rawData, context, profile);
	}

	@Override
	public ObjectNode write(MessageFileData data, FormatContext context) {
		ObjectNode output = new ObjectNode();
		if (data == null || data.getEntries().isEmpty() || context == null) return output;

		Locale defaultLocale = context.getDefaultLocale();
		MessageFormatUtil.LocaleMatch match = MessageFormatUtil.detectLocaleFromPath(
				context.getRoot(),
				context.getFile(),
				defaultLocale
		);
		if (match == null || match.locale() == null) return output;

		String localeKey = LocaleUtil.formatLocale(match.locale());
		LocaleFormatProfile profile = new LocaleFormatProfile(localeKey, "");
		return CODEC.write(data, context, profile);
	}

	@Override
	public boolean canRepresent(MessageFileData.Entry entry) {
		if (entry == null) return false;
		if (entry.getExtensions() != null && !entry.getExtensions().isEmpty()) return false;
		if (entry.getText() != null) return false;

		return entry.getLocales() != null && !entry.getLocales().isEmpty();
	}
}
