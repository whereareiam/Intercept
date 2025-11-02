package me.whereareiam.intercept.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

	/**
	 * Update checker configuration
	 */
	private Updater updater;

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
}
