package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.model.regex.MatchDetails;

import java.util.Locale;
import java.util.Optional;

/**
 * Service for matching text against message regex patterns.
 * Provides optimization through pattern indexing, literal prefix checking, and caching.
 */
public interface RegexMatchingService {
	/**
	 * Try to match text against all registered regex patterns.
	 * Returns the resolved message if a match is found.
	 *
	 * @param text   the text to match
	 * @param locale the locale for message resolution
	 * @return the resolved message, or empty if no match
	 */
	Optional<String> match(String text, Locale locale);

	/**
	 * Try to match text against all registered regex patterns and return match details.
	 * This method returns detailed information needed for component replacement with formatting preservation.
	 *
	 * @param text   the text to match
	 * @param locale the locale for message resolution
	 * @return match details including resolved text and match positions, or empty if no match
	 */
	Optional<MatchDetails> matchWithDetails(String text, Locale locale);
}