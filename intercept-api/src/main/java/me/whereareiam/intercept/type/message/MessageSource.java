package me.whereareiam.intercept.type.message;

/**
 * Source context for message resolution.
 * Used to determine appropriate fallback formatting when translations are missing.
 */
public enum MessageSource {
	/**
	 * Message originated from chat (player chat messages).
	 */
	CHAT,

	/**
	 * Unknown or unspecified source.
	 */
	UNKNOWN
}