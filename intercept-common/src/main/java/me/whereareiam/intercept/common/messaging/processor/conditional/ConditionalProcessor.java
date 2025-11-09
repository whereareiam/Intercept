package me.whereareiam.intercept.common.messaging.processor.conditional;

import me.whereareiam.intercept.common.util.MessageTags;

import java.util.Map;

/**
 * Processes conditional tags in messages.
 * Syntax: <if condition>true-text<else>false-text</if>
 */
public class ConditionalProcessor {
	/**
	 * Process conditionals in text.
	 *
	 * @param text         the text containing conditionals
	 * @param placeholders the placeholder values for evaluation
	 * @return text with conditionals evaluated
	 */
	public String process(String text, Map<String, Object> placeholders) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		if (!text.contains(MessageTags.CONDITIONAL_IF_TAG)) {
			return text; // Fast path: no conditionals
		}

		// Process conditionals recursively (for nested conditionals)
		String result = text;
		int maxIterations = 10; // Prevent infinite loops
		int iteration = 0;

		while (result.contains(MessageTags.CONDITIONAL_IF_TAG) && iteration < maxIterations) {
			String before = result;
			result = processOnce(result, placeholders);
			if (result.equals(before)) {
				break; // No more changes
			}
			iteration++;
		}

		return result;
	}

	private String processOnce(String text, Map<String, Object> placeholders) {
		// Find the first <if> tag
		int ifStart = text.indexOf(MessageTags.CONDITIONAL_IF_TAG);
		if (ifStart == -1) {
			return text;
		}

		// Find the closing > of the <if> tag (must be followed by content, not an operator value)
		// Look for > followed by a letter, <, or special char (not a number from operator)
		int conditionEnd = -1;
		int ifTagLength = MessageTags.CONDITIONAL_IF_TAG.length();
		for (int i = ifStart + ifTagLength; i < text.length(); i++) {
			if (text.charAt(i) == '>' && i + 1 < text.length()) {
				char next = text.charAt(i + 1);
				// Check if this > closes the tag (followed by content, not part of operator)
				if (Character.isLetter(next) || next == '<' || next == '§' || next == '[' ||
						next == '(' || next == '{' || next == '"' || next == '\'' || Character.isWhitespace(next)) {
					conditionEnd = i;
					break;
				}
			}
		}

		if (conditionEnd == -1) {
			return text;
		}

		String condition = text.substring(ifStart + ifTagLength, conditionEnd).trim();

		// Find matching </if> with proper nesting
		int nestLevel = 1;
		int pos = conditionEnd + 1;
		int elsePos = -1;

		while (pos < text.length() && nestLevel > 0) {
			if (text.startsWith(MessageTags.CONDITIONAL_IF_TAG, pos)) {
				nestLevel++;
				pos += MessageTags.CONDITIONAL_IF_TAG.length();
			} else if (text.startsWith(MessageTags.CONDITIONAL_IF_CLOSE_TAG, pos)) {
				nestLevel--;
				if (nestLevel == 0) {
					break;
				}
				pos += MessageTags.CONDITIONAL_IF_CLOSE_TAG.length();
			} else if (text.startsWith(MessageTags.CONDITIONAL_ELSE_TAG, pos) && nestLevel == 1 && elsePos == -1) {
				elsePos = pos;
				pos += MessageTags.CONDITIONAL_ELSE_TAG.length();
			} else {
				pos++;
			}
		}

		if (nestLevel != 0) {
			// Unmatched <if>, return original
			return text;
		}

		int ifEnd = pos;
		String ifBlock;
		String elseBlock = null;

		if (elsePos != -1) {
			ifBlock = text.substring(conditionEnd + 1, elsePos);
			elseBlock = text.substring(elsePos + MessageTags.CONDITIONAL_ELSE_TAG.length(), ifEnd);
		} else {
			ifBlock = text.substring(conditionEnd + 1, ifEnd);
		}

		boolean conditionResult = evaluateCondition(condition, placeholders);
		String replacement = conditionResult ? ifBlock : (elseBlock != null ? elseBlock : "");

		return text.substring(0, ifStart) + replacement + text.substring(ifEnd + MessageTags.CONDITIONAL_IF_CLOSE_TAG.length());
	}

	private boolean evaluateCondition(String condition, Map<String, Object> placeholders) {
		condition = condition.trim();

		// Try each operator in order (longest/most specific first to avoid conflicts)
		for (ConditionalOperator operator : ConditionalOperator.values())
			if (operator.matches(condition)) return operator.evaluate(condition, placeholders);

		return false;
	}
}