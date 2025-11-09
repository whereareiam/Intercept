package me.whereareiam.intercept.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Utility class for working with Adventure Components.
 * Provides methods to extract text and perform replacements while preserving formatting.
 */
public final class ComponentHelper {
	private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

	/**
	 * Extract plain text from a component, removing all formatting.
	 * Used for pattern matching and tag detection.
	 *
	 * @param component the component to extract text from
	 * @return the plain text content
	 */
	public static String extractPlainText(Component component) {
		return PLAIN_SERIALIZER.serialize(component);
	}

	/**
	 * Check if a component's text contains a specific tag.
	 *
	 * @param component the component to check
	 * @param tag       the tag to look for (e.g., "&lt;lang&gt;")
	 * @return true if the component contains the tag
	 */
	public static boolean containsTag(Component component, String tag) {
		return extractPlainText(component).contains(tag);
	}

	/**
	 * Replace the entire text content of a component with new text.
	 * This creates a new text component, discarding original formatting.
	 * Use this when you want to completely replace the message.
	 *
	 * @param newText the new text to use
	 * @return a new text component with the new text
	 */
	public static Component replaceEntireText(String newText) {
		return Component.text(newText);
	}
}