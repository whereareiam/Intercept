package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

/**
 * Represents a single message entry from a file.
 */
@Getter
@Setter
public class MessageDocumentEntry {
	/**
	 * Single text (String or List&lt;String&gt;).
	 * Used for templates or non-localized messages.
	 */
	private Object text;

	/**
	 * Locale translations grouped under "locales".
	 * Keys are locale strings or "default".
	 */
	private Map<String, Object> locales;

	public void setLocales(Map<String, Object> locales) {
		this.locales = locales == null || locales.isEmpty() ? null : locales;
	}
}
