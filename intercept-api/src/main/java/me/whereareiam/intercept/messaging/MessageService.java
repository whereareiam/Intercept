package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.model.MessageRequest;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Main service for resolving messages.
 */
public interface MessageService {
	/**
	 * Resolves a message with full context.
	 *
	 * @param request the message request
	 * @return the resolved message text
	 */
	String resolve(MessageRequest request);

	/**
	 * Resolves a message by key and locale with placeholders.
	 *
	 * @param key          the message key
	 * @param locale       the locale
	 * @param placeholders the placeholders
	 * @return the resolved message text
	 */
	String resolve(String key, Locale locale, Map<String, Object> placeholders);

	/**
	 * Resolves a message by key and locale without placeholders.
	 *
	 * @param key    the message key
	 * @param locale the locale
	 * @return the resolved message text
	 */
	String resolve(String key, Locale locale);

	/**
	 * Check if a message key exists.
	 *
	 * @param key the message key
	 * @return true if exists
	 */
	boolean exists(String key);

	/**
	 * Get all available locales for a message key.
	 *
	 * @param key the message key
	 * @return set of available locale codes
	 */
	Set<String> getAvailableLocales(String key);
}


