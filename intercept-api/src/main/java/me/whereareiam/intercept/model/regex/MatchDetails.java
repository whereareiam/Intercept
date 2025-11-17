package me.whereareiam.intercept.model.regex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of a regex match with component replacement details.
 *
 * @param resolvedText   the resolved message text
 * @param matchStart     start index of the match in the original text (if replaceMatched is true)
 * @param matchEnd       end index of the match in the original text (if replaceMatched is true)
 * @param replaceMatched whether only the matched part should be replaced
 */
@Getter
@AllArgsConstructor
public class MatchDetails {
	private final String resolvedText;
	private final int matchStart;
	private final int matchEnd;
	private final boolean replaceMatched;
}