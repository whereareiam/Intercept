package me.whereareiam.intercept.common.messaging.processor.conditional;

import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Enum representing conditional operators with their evaluation logic.
 * Ordered by priority (longest/most specific operators first).
 */
public enum ConditionalOperator {
	// Two-character operators (check first to avoid conflicts)
	EQUALS("==", 2, ConditionalOperator::evaluateEquals),
	NOT_EQUALS("!=", 2, ConditionalOperator::evaluateNotEquals),
	GREATER_OR_EQUALS(">=", 2, ConditionalOperator::evaluateGreaterOrEquals),
	LESS_OR_EQUALS("<=", 2, ConditionalOperator::evaluateLessOrEquals),
	CONTAINS("~=", 2, ConditionalOperator::evaluateContains),
	STARTS_WITH("^=", 2, ConditionalOperator::evaluateStartsWith),
	ENDS_WITH("$=", 2, ConditionalOperator::evaluateEndsWith),

	// Single-character operators (check after two-character ones)
	GREATER_THAN(">", 2, ConditionalOperator::evaluateGreaterThan),
	LESS_THAN("<", 2, ConditionalOperator::evaluateLessThan),

	// Suffix operators (check last, no splits)
	EMPTY_CHECK("!?", 1, ConditionalOperator::evaluateEmpty),
	NOT_EMPTY_CHECK("?", 1, ConditionalOperator::evaluateNotEmpty);

	private final String symbol;
	private final int operandCount; // 1 for unary (suffix), 2 for binary (infix)
	private final BiPredicate<String, Map<String, Object>> evaluator;

	ConditionalOperator(String symbol, int operandCount, BiPredicate<String, Map<String, Object>> evaluator) {
		this.symbol = symbol;
		this.operandCount = operandCount;
		this.evaluator = evaluator;
	}

	public boolean isSuffixOperator() {
		return operandCount == 1;
	}

	public boolean matches(String condition) {
		if (isSuffixOperator())
			return condition.endsWith(symbol);

		return condition.contains(symbol);
	}

	public boolean evaluate(String condition, Map<String, Object> placeholders) {
		return evaluator.test(condition, placeholders);
	}

	// Evaluation methods for each operator
	private static boolean evaluateEquals(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("==", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return left.equals(right);
	}

	private static boolean evaluateNotEquals(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("!=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return !left.equals(right);
	}

	private static boolean evaluateGreaterThan(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split(">", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return compareNumeric(left, right) > 0;
	}

	private static boolean evaluateGreaterOrEquals(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split(">=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return compareNumeric(left, right) >= 0;
	}

	private static boolean evaluateLessThan(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("<", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return compareNumeric(left, right) < 0;
	}

	private static boolean evaluateLessOrEquals(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("<=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return compareNumeric(left, right) <= 0;
	}

	private static boolean evaluateContains(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("~=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return left.contains(right);
	}

	private static boolean evaluateStartsWith(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("\\^=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return left.startsWith(right);
	}

	private static boolean evaluateEndsWith(String condition, Map<String, Object> placeholders) {
		String[] parts = condition.split("\\$=", 2);
		String left = getPlaceholderValue(parts[0].trim(), placeholders);
		String right = parts[1].trim();

		return left.endsWith(right);
	}

	private static boolean evaluateNotEmpty(String condition, Map<String, Object> placeholders) {
		String varName = condition.substring(0, condition.length() - 1).trim();
		String value = getPlaceholderValue(varName, placeholders);

		return !value.isEmpty();
	}

	private static boolean evaluateEmpty(String condition, Map<String, Object> placeholders) {
		String varName = condition.substring(0, condition.length() - 2).trim();
		String value = getPlaceholderValue(varName, placeholders);

		return value.isEmpty();
	}

	// Helper methods
	private static String getPlaceholderValue(String name, Map<String, Object> placeholders) {
		Object value = placeholders.get(name);
		return value != null ? String.valueOf(value) : "";
	}

	private static int compareNumeric(String left, String right) {
		try {
			double leftNum = Double.parseDouble(left);
			double rightNum = Double.parseDouble(right);

			return Double.compare(leftNum, rightNum);
		} catch (NumberFormatException e) {
			// Fall back to string comparison
			return left.compareTo(right);
		}
	}
}

