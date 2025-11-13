package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.annotation.PostProcess;
import me.whereareiam.intercept.logging.InterceptionHelper;
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
		 * Regex matching configuration
		 */
		private Regex regex;

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

		/**
		 * Regex matching configuration for performance optimization.
		 */
		@Getter
		@Setter
		@ToString
		public static class Regex {
			/**
			 * Whether regex matching is enabled globally
			 */
			private boolean enabled;

			/**
			 * Timeout in milliseconds for each pattern match
			 */
			private int timeoutMs;

			/**
			 * Whether to use literal prefix optimization
			 */
			private boolean useLiteralPrefix;

			/**
			 * Whether to cache regex match results
			 */
			private boolean cacheResults;

			/**
			 * Maximum number of cached regex results
			 */
			private int cacheSize;

			/**
			 * Cache expiration in minutes
			 */
			private int cacheExpireMinutes;

			/**
			 * Maximum pattern complexity score
			 */
			private int maxPatternComplexity;

			/**
			 * Log warning for patterns taking longer than this (ms)
			 */
			private int warnSlowPatternsMs;
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
}