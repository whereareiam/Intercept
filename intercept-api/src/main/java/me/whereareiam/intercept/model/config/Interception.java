package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.type.InterceptedComponentType;

import java.util.Map;

/**
 * Configuration for intercepting different Minecraft components.
 * Uses a map-based structure with enum keys for type-safe component registration.
 *
 * <p>Components are configured using enum keys:</p>
 * <pre>
 * interception:
 *   CHAT:
 *     enabled: true
 *     tag: "&lt;lang&gt;"
 * </pre>
 */
@Getter
@Setter
@ToString
public class Interception {
	/**
	 * Map of component type to component configuration.
	 * Keys are InterceptedComponentType enum values (e.g., CHAT).
	 * Values are InterceptedComponent instances or their extensions.
	 */
	private Map<InterceptedComponentType, InterceptedComponent> components;
}