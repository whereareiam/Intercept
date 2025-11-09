package me.whereareiam.intercept.common.util;

/**
 * Centralized definition of all custom message tags.
 * Change tag names here to apply across the entire system.
 */
public final class MessageTags {
	/**
	 * Message reference tag prefix.
	 * Used to reference other messages/templates: {@code <m:key>}
	 */
	public static final String MESSAGE_REF_PREFIX = "m";

	/**
	 * Placeholder tag prefix.
	 * Used for dynamic values: {@code <p:name>}
	 */
	public static final String PLACEHOLDER_PREFIX = "p";

	/**
	 * Template tag prefix.
	 * Used to invoke templates: {@code <tpl:name param='value'>}
	 */
	public static final String TEMPLATE_PREFIX = "tpl";

	/**
	 * Conditional tag name.
	 * Used for conditional logic: {@code <if condition>...<else>...</if>}
	 */
	public static final String CONDITIONAL_IF = "if";

	/**
	 * Conditional else tag name.
	 * Used in conditional blocks: {@code <else>}
	 */
	public static final String CONDITIONAL_ELSE = "else";

	// Full tag patterns (for convenience)

	/**
	 * Full message reference tag: {@code <m:}
	 */
	public static final String MESSAGE_REF_TAG = "<" + MESSAGE_REF_PREFIX + ":";

	/**
	 * Full placeholder tag: {@code <p:}
	 */
	public static final String PLACEHOLDER_TAG = "<" + PLACEHOLDER_PREFIX + ":";

	/**
	 * Full template tag: {@code <tpl:}
	 */
	public static final String TEMPLATE_TAG = "<" + TEMPLATE_PREFIX + ":";

	/**
	 * Full conditional if tag: {@code <if }
	 */
	public static final String CONDITIONAL_IF_TAG = "<" + CONDITIONAL_IF + " ";

	/**
	 * Full conditional else tag: {@code <else>}
	 */
	public static final String CONDITIONAL_ELSE_TAG = "<" + CONDITIONAL_ELSE + ">";

	/**
	 * Conditional if closing tag: {@code </if>}
	 */
	public static final String CONDITIONAL_IF_CLOSE_TAG = "</" + CONDITIONAL_IF + ">";
}