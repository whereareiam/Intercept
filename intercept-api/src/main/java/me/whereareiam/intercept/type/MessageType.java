package me.whereareiam.intercept.type;

/**
 * Type of message entry.
 */
public enum MessageType {
	/**
	 * A regular message with translations.
	 */
	MESSAGE,

	/**
	 * A template that can be referenced by other messages.
	 */
	TEMPLATE,

	/**
	 * A mixed file containing both messages and templates.
	 */
	MIXED
}