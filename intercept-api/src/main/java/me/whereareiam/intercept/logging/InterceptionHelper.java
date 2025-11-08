package me.whereareiam.intercept.logging;

import lombok.Getter;
import net.kyori.adventure.text.Component;

/**
 * Static utility class for controlling interception testing features.
 * <p>
 * Testing mode is enabled when the logging level is higher than 2,
 * which controls testing features like the [INTERCEPTED] tag in chat messages.
 * <p>
 * Initialize by calling {@link #init(boolean)} during plugin bootstrap.
 */
public class InterceptionHelper {
	@Getter
	private static boolean testingMode;

	/**
	 * Initializes the interception testing mode state.
	 * Must be called during plugin initialization.
	 *
	 * @param enabled whether testing mode is enabled (level > 2)
	 */
	public static void init(boolean enabled) {
		InterceptionHelper.testingMode = enabled;
	}

	/**
	 * Appends the [INTERCEPTED] tag to the given component if testing mode is enabled.
	 * If testing mode is disabled, returns the original component unchanged.
	 *
	 * @param component the component to potentially append the tag to
	 * @return the component with [INTERCEPTED] tag appended if testing mode is enabled, or the original component otherwise
	 */
	public static Component modify(Component component) {
		if (testingMode) return component.append(Component.text(" [INTERCEPTED]"));

		return component;
	}
}