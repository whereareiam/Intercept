package me.whereareiam.intercept.model.regex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of a regex match with component replacement details.
 */
@Getter
@AllArgsConstructor
public class MatchDetails {
	private final String resolvedText;
	private final int matchStart;
	private final int matchEnd;
	private final boolean replaceMatched;
}