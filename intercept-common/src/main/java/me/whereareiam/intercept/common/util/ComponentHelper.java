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

	/**
	 * Replace text with Component values.
	 * When text matches, replaces it with the new Component preserving structure.
	 *
	 * @param component    the component to process
	 * @param replacements map of old text -> new Component
	 * @return new component with replacements applied
	 */
	public static Component replaceTextWithComponents(Component component, Map<String, Component> replacements) {
		if (replacements.isEmpty()) return component;

		return traverseAndReplaceWithComponents(component, replacements);
	}

	/**
	 * Recursively traverse and replace text nodes with Components.
	 */
	private static Component traverseAndReplaceWithComponents(Component component, Map<String, Component> replacements) {
		if (component instanceof TextComponent textComponent)
			return processTextComponent(textComponent, replacements);

		if (component instanceof TranslatableComponent translatable)
			return processTranslatableComponent(translatable, replacements);

		return processGenericComponent(component, replacements);
	}

	/**
	 * Process a TextComponent for replacements.
	 */
	private static Component processTextComponent(TextComponent textComponent, Map<String, Component> replacements) {
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
			if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
				earliestIndex = index;
				earliestTarget = entry.getKey();
				earliestReplacement = entry.getValue();
			}
		}

		// Found a match - split and replace (recursion handles subsequent matches)
		if (earliestTarget != null)
			return splitAndReplaceWithComponents(textComponent, earliestTarget, earliestReplacement, replacements);

		// No match - rebuild with processed children
		return rebuildWithChildren(textComponent, replacements);
	}

	/**
	 * Replace exact match and preserve style.
	 */
	private static Component replaceExactMatch(TextComponent original, Component replacement, Map<String, Component> replacements) {
		Component styled = replacement.style(original.style());

		for (Component child : original.children())
			styled = styled.append(traverseAndReplaceWithComponents(child, replacements));

		return styled;
	}

	/**
	 * Rebuild component with processed children (no content changes).
	 */
	private static Component rebuildWithChildren(TextComponent textComponent, Map<String, Component> replacements) {
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
	private static Component processTranslatableComponent(TranslatableComponent translatable, Map<String, Component> replacements) {
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
	private static List<ComponentLike> processTranslationArguments(List<TranslationArgument> arguments, Map<String, Component> replacements) {
		List<ComponentLike> processed = new ArrayList<>();

		for (TranslationArgument arg : arguments) {
			Object value = arg.value();

			if (value instanceof Component argComponent) {
				processed.add(traverseAndReplaceWithComponents(argComponent, replacements));
				continue;
			}

			if (value instanceof ComponentLike)
				processed.add((ComponentLike) value);
		}

		return processed;
	}

	/**
	 * Process generic components by traversing their children.
	 */
	private static Component processGenericComponent(Component component, Map<String, Component> replacements) {
		List<Component> children = component.children();
		if (children.isEmpty())
			return component;

		Component result = component;
		for (Component child : children)
			result = result.append(traverseAndReplaceWithComponents(child, replacements));

		return result;
	}

	/**
	 * Split text at target position and insert replacement Component.
	 */
	private static Component splitAndReplaceWithComponents(
			TextComponent original, String target, Component replacement, Map<String, Component> allReplacements
	) {
		String content = original.content();
		int index = content.indexOf(target);
		if (index == -1) return original;

		Component result = Component.empty().style(original.style());

		// Add prefix text (before match)
		if (index > 0) result = result.append(createStyledText(content.substring(0, index), original.style()));

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
}