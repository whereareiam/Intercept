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
	 * The tag that should be processed in this component.
	 * Tag is enclosed in angle brackets, e.g., "&lt;lang&gt;".
	 */
	private String tag;
}