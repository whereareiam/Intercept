package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.type.message.MessageSource;
import net.kyori.adventure.text.Component;

import java.util.Locale;

/**
 * Service for replacing translation tags in Adventure components.
 * Handles tag parsing, message resolution, and component reconstruction while preserving formatting.
 * <p>
 * The tag format is configurable and must include delimiters and tag name.
 * Common formats:
 * <ul>
 *   <li>{@code <lang>} - Angle brackets (XML-like)</li>
 *   <li>{@code [l]} - Square brackets</li>
 *   <li>{@code {tr}} - Curly braces</li>
 * </ul>
 * <p>
 * Tag syntax: {@code tagFormat key="message.key" param1="value1" param2="value2"}
 * <p>
 * Example usage:
 * <pre>
 * Component input = Component.text("Welcome &lt;lang key=\"player.join\" name=\"Steve\"&gt;");
 * Component output = tagService.replaceTags(input, "&lt;lang&gt;", locale);
 * // Output: "Welcome Steve joined the game!" (preserving all formatting)
 *
 * // Or with custom format:
 * Component input = Component.text("Welcome [l key=\"player.join\" name=\"Steve\"]");
 * Component output = tagService.replaceTags(input, "[l]", locale);
 * </pre>
 */
public interface TagReplacementService {
	/**
	 * Replace all translation tags in a component with resolved messages.
	 * Preserves all component formatting, colors, and style.
	 * Uses UNKNOWN as the message source.
	 *
	 * @param component the component to process
	 * @param tagFormat the full tag format (e.g., "&lt;lang&gt;", "[l]", "{tr}")
	 * @param locale    the locale for message resolution
	 * @return new component with tags replaced by resolved messages
	 */
	Component replaceTags(Component component, String tagFormat, Locale locale);

	/**
	 * Replace all translation tags in a component with resolved messages.
	 * Preserves all component formatting, colors, and style.
	 * Uses source-specific fallback formatting when translations are missing.
	 *
	 * @param component the component to process
	 * @param tagFormat the full tag format (e.g., "&lt;lang&gt;", "[l]", "{tr}")
	 * @param locale    the locale for message resolution
	 * @param source    the source context of the message (CHAT, COMMAND, etc.)
	 * @return new component with tags replaced by resolved messages
	 */
	Component replaceTags(Component component, String tagFormat, Locale locale, MessageSource source);

	/**
	 * Check if a component contains any tags for the given tag format.
	 *
	 * @param component the component to check
	 * @param tagFormat the full tag format (e.g., "&lt;lang&gt;", "[l]", "{tr}")
	 * @return true if the component contains at least one tag
	 */
	boolean containsTags(Component component, String tagFormat);
}