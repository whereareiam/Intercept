package me.whereareiam.intercept.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for working with Adventure Components.
 * Provides methods to extract text and perform replacements while preserving formatting.
 * For tag parsing operations, see {@link TagParser}.
 */
public final class ComponentHelper {
	private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

	/**
	 * Extract plain text from a component, removing all formatting.
	 *
	 * @param component the component to extract text from
	 * @return the plain text content
	 */
	public static String extractPlainText(Component component) {
		return PLAIN_SERIALIZER.serialize(component);
	}

	/**
	 * Replace the entire text content of a component with a new Component.
	 * Use this when you want to completely replace the message with a formatted component.
	 *
	 * @param newComponent the new component to use
	 * @return the new component
	 */
	public static Component replaceEntireText(Component newComponent) {
		return newComponent;
	}

	/**
	 * Replace text with Component values using Adventure's built-in replacement API.
	 * When text matches, replaces it with the new Component preserving structure.
	 *
	 * @param component    the component to process
	 * @param replacements map of old text -> new Component
	 * @return new component with replacements applied
	 */
	public static Component replaceTextWithComponents(Component component, Map<String, Component> replacements) {
		if (component == null) return null;
		if (replacements == null || replacements.isEmpty()) return component;
		
		// Use Adventure's replaceText API - chain multiple replacements
		Component result = component;
		for (Map.Entry<String, Component> entry : replacements.entrySet()) {
			final String literal = entry.getKey();
			final Component replacement = entry.getValue();
			
			result = result.replaceText(config -> config
				.matchLiteral(literal)
				.replacement(replacement)
			);
		}
		
		return result;
	}

	/* ------------------------------------------------------------------------
	 * Range-based text replacement (by plain-text index)
	 * 
	 * Note: Adventure doesn't provide range-based replacement by index,
	 * so we keep this custom implementation for that specific use case.
	 * --------------------------------------------------------------------- */

	/**
	 * Replace a substring range in a component with new text, preserving formatting of non-replaced parts.
	 * The replacement text will be inserted as a plain text component.
	 *
	 * @param component   the component to modify
	 * @param startIndex  start index (inclusive) of the range to replace in plain text
	 * @param endIndex    end index (exclusive) of the range to replace in plain text
	 * @param replacement the replacement text
	 * @return a new component with the specified range replaced
	 */
	public static Component replaceTextRange(Component component, int startIndex, int endIndex, String replacement) {
		return replaceTextRange(component, startIndex, endIndex, Component.text(replacement));
	}

	/**
	 * Replace a substring range in a component with a new Component, preserving formatting of non-replaced parts.
	 * The replacement component will be inserted with its formatting preserved.
	 *
	 * @param component   the component to modify
	 * @param startIndex  start index (inclusive) of the range to replace in plain text
	 * @param endIndex    end index (exclusive) of the range to replace in plain text
	 * @param replacement the replacement component (with formatting applied)
	 * @return a new component with the specified range replaced
	 */
	public static Component replaceTextRange(Component component, int startIndex, int endIndex, Component replacement) {
		if (startIndex < 0 || endIndex < startIndex)
			throw new IllegalArgumentException("Invalid range: start=" + startIndex + ", end=" + endIndex);

		return replaceTextRangeRecursive(component, startIndex, endIndex, replacement, 0);
	}

	/**
	 * Recursively replace a text range in a component.
	 *
	 * @param component   the component to process
	 * @param startIndex  start index in plain text
	 * @param endIndex    end index in plain text
	 * @param replacement replacement component
	 * @param currentPos  current position in plain text (for tracking)
	 * @return modified component
	 */
	private static Component replaceTextRangeRecursive(
			Component component,
			int startIndex,
			int endIndex,
			Component replacement,
			int currentPos
	) {
		if (component instanceof TextComponent textComponent)
			return processTextComponentRange(textComponent, startIndex, endIndex, replacement, currentPos);

		if (component instanceof TranslatableComponent translatable)
			return processTranslatableComponentRange(translatable, startIndex, endIndex, replacement, currentPos);

		return processGenericComponentRange(component, startIndex, endIndex, replacement, currentPos);
	}

	/**
	 * Process a TextComponent for range replacement.
	 */
	private static Component processTextComponentRange(
			TextComponent textComponent,
			int startIndex,
			int endIndex,
			Component replacement,
			int currentPos
	) {
		String content = textComponent.content();
		int contentLength = content.length();
		int contentEnd = currentPos + contentLength;

		// Case 1: Range is completely before this component
		if (endIndex <= currentPos)
			return textComponent;

		// Case 2: Range is completely after this component
		if (startIndex >= contentEnd) {
			TextComponent.Builder result = Component.text()
					.content(content)
					.style(textComponent.style());

			int childPos = contentEnd;
			for (Component child : textComponent.children()) {
				result.append(replaceTextRangeRecursive(child, startIndex, endIndex, replacement, childPos));
				childPos += plainLength(child);
			}
			return result.build();
		}

		// Case 3: Range overlaps with this component
		TextComponent.Builder result = Component.text().style(textComponent.style());

		// Before range
		int beforeStart = Math.max(0, startIndex - currentPos);
		if (beforeStart > 0) {
			String before = content.substring(0, beforeStart);
			result.append(Component.text(before).style(textComponent.style()));
		}

		// Replacement component (with formatting preserved)
		result.append(replacement);

		// After range
		int afterStart = Math.min(contentLength, endIndex - currentPos);
		if (afterStart < contentLength) {
			String after = content.substring(afterStart);
			result.append(Component.text(after).style(textComponent.style()));
		}

		// Process children - they come after the content text
		int childPos = contentEnd;
		for (Component child : textComponent.children()) {
			result.append(replaceTextRangeRecursive(child, startIndex, endIndex, replacement, childPos));
			childPos += plainLength(child);
		}

		return result.build();
	}

	/**
	 * Process a TranslatableComponent for range replacement.
	 */
	private static Component processTranslatableComponentRange(
			TranslatableComponent translatable,
			int startIndex,
			int endIndex,
			Component replacement,
			int currentPos
	) {
		String serialized = PLAIN_SERIALIZER.serialize(translatable);
		int componentLength = serialized.length();
		int componentEnd = currentPos + componentLength;

		// If range doesn't overlap, return unchanged (but still process children)
		if (endIndex <= currentPos || startIndex >= componentEnd) {
			List<Component> children = translatable.children();
			if (children.isEmpty()) return translatable;

			List<ComponentLike> newChildren = new ArrayList<>(children.size());
			int childPos = componentEnd;
			for (Component child : children) {
				newChildren.add(replaceTextRangeRecursive(child, startIndex, endIndex, replacement, childPos));
				childPos += plainLength(child);
			}
			return translatable.children(newChildren);
		}

		// For translatable components, we convert to text first to do replacement
		Component textVersion = Component.text(serialized).style(translatable.style());

		return replaceTextRangeRecursive(textVersion, startIndex, endIndex, replacement, currentPos);
	}

	/**
	 * Process generic components by traversing children.
	 * For components without text content, we process children sequentially.
	 */
	private static Component processGenericComponentRange(
			Component component,
			int startIndex,
			int endIndex,
			Component replacement,
			int currentPos
	) {
		List<Component> children = component.children();
		if (children.isEmpty()) return component;

		List<ComponentLike> newChildren = new ArrayList<>(children.size());
		int pos = currentPos;
		for (Component child : children) {
			newChildren.add(replaceTextRangeRecursive(child, startIndex, endIndex, replacement, pos));
			pos += plainLength(child);
		}

		return component.children(newChildren);
	}

	private static int plainLength(Component component) {
		return PLAIN_SERIALIZER.serialize(component).length();
	}
}
