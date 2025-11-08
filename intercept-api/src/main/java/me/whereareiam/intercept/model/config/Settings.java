package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.annotation.PostProcess;
import me.whereareiam.intercept.logging.InterceptionHelper;
import me.whereareiam.intercept.model.Event;

import java.util.Map;

/**
 * Main configuration settings class for the Intercept plugin.
 * Contains all configurable options and their default values.
 *
 * <p>Configuration sections include:</p>
 * <ul>
 *   <li>Debug level for logging</li>
 *   <li>Update checker configuration</li>
 * </ul>
 */
@Getter
@Setter
@ToString
public class Settings {
	/**
	 * Debug level for logging
	 */
	private int level;

	@PostProcess
	public void updateInterceptionHelper() {
		InterceptionHelper.init(level > 2);
	}

	/**
	 * Update checker configuration
	 */
	private Updater updater;

	/**
	 * Event listener configurations
	 */
	private Listeners listeners;

	/**
	 * Configuration for the plugin's updater checker.
	 * Controls updater notifications and checking behavior.
	 */
	@Getter
	@Setter
	@ToString
	public static class Updater {
		/**
		 * Whether to check for plugin updates
		 */
		private boolean checkForUpdates;

		/**
		 * Whether to show updater notifications
		 */
		private boolean warnAboutUpdates;

		/**
		 * Whether to warn about local builds
		 */
		private boolean warnAboutLocalBuilds;

		/**
		 * Whether to warn about development builds
		 */
		private boolean warnAboutDevBuilds;

		/**
		 * Update check interval in minutes
		 */
		private int interval;
	}

	/**
	 * Configuration for event listeners.
	 * Maps event names to their corresponding event configurations.
	 */
	@Getter
	@Setter
	@ToString
	public static class Listeners {
		/**
		 * Map of event name to event configuration
		 */
		private Map<String, Event> events;
	}
}
