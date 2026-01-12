package me.whereareiam.intercept.common.persistence.format.type.locale;

import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.NullNode;
import me.whereareiam.intercept.persistence.format.MessageFormatProfile;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Profile for LOCALE format (flattened locale-specific entries).
 */
public final class LocaleFormatProfile implements MessageFormatProfile {
	private final String localeKey;
	private final String prefix;

	public LocaleFormatProfile(String localeKey, String prefix) {
		this.localeKey = localeKey == null ? "" : localeKey;
		this.prefix = prefix == null ? "" : prefix;
	}

	@Override
	public boolean isFlattened() {
		return true;
	}

	@Override
	public String buildPrefix(FormatContext context) {
		return prefix;
	}

	@Override
	public MessageFileData.Entry parseFlattenedValue(MessageValue value, FormatContext context) {
		if (value == null || localeKey.isEmpty()) return null;
		MessageFileData.Entry entry = new MessageFileData.Entry();

		Map<String, MessageValue> locales = new LinkedHashMap<>();
		locales.put(localeKey, value);
		entry.setLocales(locales);

		return entry;
	}

	@Override
	public Node writeEntry(MessageFileData.Entry entry, FormatContext context) {
		if (entry == null || localeKey.isEmpty()) return NullNode.instance();

		Map<String, MessageValue> locales = entry.getLocales();
		if (locales == null || locales.isEmpty()) return NullNode.instance();

		MessageValue value = locales.get(localeKey);
		if (value == null) return NullNode.instance();

		return MessageFormatUtil.toNodeValue(value);
	}
}
