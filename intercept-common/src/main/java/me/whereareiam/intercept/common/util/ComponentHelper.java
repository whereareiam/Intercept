package me.whereareiam.intercept.common.util;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
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
	 * Replace text with Component values.
	 * When text matches, replaces it with the new Component preserving structure.
	 *
	 * @param component    the component to process
	 * @param replacements map of old text -> new Component
	 * @return new component with replacements applied
	 */
	public static Component replaceTextWithComponents(Component component, Map<String, Component> replacements) {
		if (component == null) return null;
		if (replacements == null || replacements.isEmpty()) return component;
		return traverseAndReplaceWithComponents(component, replacements);
	}

	/* ------------------------------------------------------------------------
	 * Text -> Component replacement (by content)
	 * --------------------------------------------------------------------- */

	/**
	 * Recursively traverse and replace text nodes with Components.
	 */
	private static Component traverseAndReplaceWithComponents(
			Component component, Map<String, Component> replacements
	) {
		if (component instanceof TextComponent textComponent)
			return processTextComponent(textComponent, replacements);

		if (component instanceof TranslatableComponent translatable)
			return processTranslatableComponent(translatable, replacements);

		return processGenericComponent(component, replacements);
	}

	/**
	 * Process a TextComponent for replacements.
	 */
	private static Component processTextComponent(
			TextComponent textComponent, Map<String, Component> replacements
	) {
		String content = textComponent.content();

		// Exact match - replace entire component
		if (replacements.containsKey(content))
			return replaceExactMatch(textComponent, replacements.get(content), replacements);

		// Partial match - find the LEFTMOST match to process in order
		String earliestTarget = null;
		Component earliestReplacement = null;
		int earliestIndex = -1;

		for (Map.Entry<String, Component> entry : replacements.entrySet()) {
			int index = content.indexOf(entry.getKey());
			if (index == -1) continue;
			if (earliestIndex == -1 || index < earliestIndex) {
				earliestIndex = index;
				earliestTarget = entry.getKey();
				earliestReplacement = entry.getValue();
			}
		}

		if (earliestTarget != null)
			return splitAndReplaceWithComponents(textComponent, earliestTarget, earliestReplacement, replacements);

		return rebuildTextWithChildren(textComponent, replacements);
	}

	/**
	 * Replace exact match and preserve style & children.
	 */
	private static Component replaceExactMatch(
			TextComponent original, Component replacement, Map<String, Component> replacements
	) {
		Component styled = replacement.style(original.style());
		return replaceChildren(styled, original.children(), replacements);
	}

	/**
	 * Rebuild TextComponent with unchanged content and processed children.
	 */
	private static Component rebuildTextWithChildren(
			TextComponent textComponent, Map<String, Component> replacements
	) {
		TextComponent.Builder builder = Component.text()
				.content(textComponent.content())
				.style(textComponent.style());

		for (Component child : textComponent.children())
			builder.append(traverseAndReplaceWithComponents(child, replacements));

		return builder.build();
	}

	/**
	 * Process a TranslatableComponent for replacements.
	 */
	private static Component processTranslatableComponent(
			TranslatableComponent translatable, Map<String, Component> replacements
	) {
		List<ComponentLike> processedArgs = processTranslationArguments(translatable.arguments(), replacements);

		TranslatableComponent.Builder builder = Component.translatable()
				.key(translatable.key())
				.style(translatable.style());

		if (!processedArgs.isEmpty())
			builder.arguments(processedArgs);

		for (Component child : translatable.children())
			builder.append(traverseAndReplaceWithComponents(child, replacements));

		return builder.build();
	}

	/**
	 * Process translation arguments recursively.
	 */
	private static List<ComponentLike> processTranslationArguments(
			List<TranslationArgument> arguments, Map<String, Component> replacements
	) {
		List<ComponentLike> processed = new ArrayList<>();

		for (TranslationArgument arg : arguments) {
			Object value = arg.value();

			if (value instanceof Component argComponent) {
				processed.add(traverseAndReplaceWithComponents(argComponent, replacements));
				continue;
			}

			if (value instanceof ComponentLike like)
				processed.add(like);
		}

		return processed;
	}

	/**
	 * Process generic components by traversing their children.
	 */
	private static Component processGenericComponent(Component component, Map<String, Component> replacements) {
		List<Component> children = component.children();
		if (children.isEmpty()) return component;

		return replaceChildren(component, children, replacements);
	}

	/**
	 * Split text at target position and insert replacement Component.
	 */
	private static Component splitAndReplaceWithComponents(
			TextComponent original,
			String target,
			Component replacement,
			Map<String, Component> allReplacements
	) {
		String content = original.content();
		int index = content.indexOf(target);
		if (index == -1) return original;

		Component result = Component.empty().style(original.style());

		// Add prefix text (before match)
		if (index > 0)
			result = result.append(createStyledText(content.substring(0, index), original.style()));

		// Add replacement component
		result = result.append(replacement);

		// Add suffix text (after match) - recursively check for more replacements
		if (index + target.length() < content.length()) {
			String after = content.substring(index + target.length());
			Component afterComponent = createStyledText(after, original.style());
			result = result.append(traverseAndReplaceWithComponents(afterComponent, allReplacements));
		}

		// Append processed children
		for (Component child : original.children())
			result = result.append(traverseAndReplaceWithComponents(child, allReplacements));

		return result;
	}

	/**
	 * Create a text component with the given style.
	 */
	private static Component createStyledText(String text, Style style) {
		return Component.text(text).style(style);
	}

	/**
	 * Helper: rebuild a component with the same content but new, processed children.
	 */
	private static Component replaceChildren(
			Component parent,
			List<Component> originalChildren,
			Map<String, Component> replacements
	) {
		if (originalChildren.isEmpty()) return parent;

		List<ComponentLike> newChildren = new ArrayList<>(originalChildren.size());
		for (Component child : originalChildren)
			newChildren.add(traverseAndReplaceWithComponents(child, replacements));

		return parent.children(newChildren);
	}

	/* ------------------------------------------------------------------------
	 * Range-based text replacement (by plain-text index)
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