package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.model.Event;

import java.util.Locale;
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

	/**
	 * Default locale for messages
	 */
	private Locale locale;

	/**
	 * Serialization configuration
	 */
	private Serialization serialization;

	/**
	 * Update checker configuration
	 */
	private Updater updater;

	/**
	 * Event listener configurations
	 */
	private Listeners listeners;

	/**
	 * Performance configuration
	 */
	private Performance performance;

	/**
	 * Command configuration
	 */
	private Commands commands;

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

	/**
	 * Performance configuration for the plugin.
	 */
	@Getter
	@Setter
	@ToString
	public static class Performance {
		/**
		 * Cache configuration
		 */
		private Cache cache;

		/**
		 * Whether to pre-render static messages at load time
		 */
		private boolean prerenderStatic;

		/**
		 * Whether to build dependency graph for optimization
		 */
		private boolean buildDependencyGraph;

		/**
		 * Cache configuration for messages.
		 */
		@Getter
		@Setter
		@ToString
		public static class Cache {
			/**
			 * Whether caching is enabled
			 */
			private boolean enabled;

			/**
			 * Maximum size for semi-static cache
			 */
			private int semiStaticSize;

			/**
			 * Maximum size for dynamic cache
			 */
			private int dynamicSize;

			/**
			 * Cache expiration in minutes for semi-static cache
			 */
			private int semiStaticExpireMinutes;

			/**
			 * Cache expiration in minutes for dynamic cache
			 */
			private int dynamicExpireMinutes;

			/**
			 * Cache expiration in minutes for render cache
			 */
			private int expireMinutes;
		}
	}

	/**
	 * Command configuration for the plugin.
	 */
	@Getter
	@Setter
	@ToString
	public static class Commands {
		/**
		 * Whether to use modern Brigadier-based command system (Paper 1.20.5+)
		 */
		private boolean useBrigadier;

		/**
		 * Whether to register asynchronous completions when available
		 */
		private boolean useAsyncCompletions;
	}

	/**
	 * Serialization configuration for message formatting.
	 * Controls how messages are serialized and formatted.
	 */
	@Getter
	@Setter
	@ToString
	public static class Serialization {
		/**
		 * Serializer adapter ID/type to use.
		 * Available options: "MINIMESSAGE", "GSON", "LEGACY_AMPERSAND", "LEGACY_SECTION", "PLAIN"
		 * Default: "MINIMESSAGE"
		 */
		private String type = "MINIMESSAGE";

		/**
		 * Whether to enable legacy color code parsing (& and § codes).
		 * When enabled, legacy codes in input will be converted to the target adapter format.
		 * Default: false
		 */
		private boolean enableLegacyColors = false;
	}
}
