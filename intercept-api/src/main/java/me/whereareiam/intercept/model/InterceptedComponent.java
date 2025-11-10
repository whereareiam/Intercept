package me.whereareiam.intercept.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base configuration class for interceptable Minecraft components.
 * Contains common settings that all components share.
 * Extend this class to add component-specific fields.
 */
@Getter
@Setter
@ToString
public class InterceptedComponent {
	/**
	 * Whether to intercept this component
	 */
	private boolean enabled;

	/**
	 * The tag format that should be processed in this component.
	 * Must include both the opening delimiter, tag name, and closing delimiter.
	 * <p>
	 * Common examples:
	 * <ul>
	 *   <li>{@code <lang>} - Angle brackets (XML-like)</li>
	 *   <li>{@code [l]} - Square brackets</li>
	 *   <li>{@code {tr}} - Curly braces</li>
	 *   <li>{@code (translate)} - Parentheses</li>
	 * </ul>
	 * <p>
	 * The format is flexible - you can use any single character as delimiters
	 * and any name for the tag.
	 * <p>
	 * Usage in messages: {@code <lang key="message.key" param1="value1">}
	 */
	private String tag;

	/**
	 * Whether to enable regex pattern matching for this component.
	 * When enabled, messages without tags will be matched against regex patterns.
	 */
	private boolean regex = false;
}