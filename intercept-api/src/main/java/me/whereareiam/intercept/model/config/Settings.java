package me.whereareiam.intercept.model.config;

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
	 * Namespace visibility configuration
	 */
	private Namespaces namespaces;

	/**
	 * Update checker configuration
	 */
	private Updater updater;

	/**
	 * Performance configuration
	 */
	private Performance performance;

	/**
	 * Command configuration
	 */
	private Commands commands;

	/**
	 * Translation configuration
	 */
	private Translation translation;

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
	 * Configuration for message namespaces.
	 */
	@Getter
	@Setter
	@ToString
	public static class Namespaces {
		/**
		 * Whether to append namespaces to exposed keys.
		 */
		private boolean appendToKeys;
	}

	/**
	 * Configuration for translation tag processing.
	 */
	@Getter
	@Setter
	@ToString
	public static class Translation {
		/**
		 * Tag format for internal Intercept messages (commands, etc.)
		 * <p>
		 * Examples: "{@code <lang>}", "{@code [tr]}", "{@code {i18n}}"
		 * <p>
		 * Note: Component interception (chat, action bar, etc.) uses per-component
		 * tag configuration in the Interception section.
		 */
		private String tagFormat;

		/**
		 * Whether to automatically process translation tags in Serializer.
		 * <p>
		 * When enabled, messages containing translation tags will be automatically
		 * translated during serialization through the MessageDecorator pipeline.
		 */
		private boolean autoProcess;
	}
}
