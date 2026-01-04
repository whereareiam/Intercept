package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.type.MultiValue;

import java.util.Map;

/**
 * Represents a single message entry from a file.
 */
@Getter
@Setter
public class MessageDocumentEntry {
	/**
	 * Single text (string or list).
	 * Used for templates or non-localized messages.
	 */
	private MultiValue<String> text;

	/**
	 * Locale translations grouped under "locales".
	 * Keys are locale strings or "default".
	 */
	private Map<String, MultiValue<String>> locales;

	/**
	 * Interception rules for this entry.
	 */
	private MessageDocumentInterception interception;

	public void setLocales(Map<String, MultiValue<String>> locales) {
		this.locales = locales == null || locales.isEmpty() ? null : locales;
	}

	public void setInterception(MessageDocumentInterception interception) {
		this.interception = (interception == null || interception.getPatterns() == null
				|| interception.getPatterns().isEmpty()) ? null : interception;
	}
}
