package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.messaging.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;

import java.util.List;
import java.util.Set;

/**
 * A message entry loaded from configuration.
 */
public interface MessageEntry {
	/**
	 * Get the type of this entry.
	 *
	 * @return the message type
	 */
	MessageType getType();

	/**
	 * Get text for a specific locale with fallback.
	 * Fallback chain: requested locale → default locale → message key
	 *
	 * @param locale        the requested locale
	 * @param defaultLocale the fallback locale (from Settings)
	 * @param messageKey    the message key (returned if no translation found)
	 * @return the text, or the message key if not found
	 */
	String getText(String locale, String defaultLocale, String messageKey);

	/**
	 * Get text for a specific locale.
	 *
	 * @param locale the locale
	 * @return the text, or null if not available
	 */
	String getText(String locale);

	/**
	 * Get text without locale (for single-language messages).
	 *
	 * @return the text, or null if this is a multi-language message
	 */
	String getText();

	/**
	 * Get all available locales for this entry.
	 *
	 * @return set of locale codes
	 */
	Set<String> getLocales();

	/**
	 * Check if this entry has translations.
	 *
	 * @return true if multi-language, false if single-language
	 */
	boolean hasTranslations();

	/**
	 * Get regex patterns that can trigger this message.
	 *
	 * @return list of compiled regex patterns, or empty list if none
	 */
	List<CompiledRegexPattern> getRegexPatterns();

	/**
	 * Check if this entry has regex patterns.
	 *
	 * @return true if this entry has regex patterns
	 */
	boolean hasRegexPatterns();
}