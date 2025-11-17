package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.commandant.model.message.ExceptionMessages;
import me.whereareiam.commandant.model.message.HelpMessages;
import me.whereareiam.commandant.model.message.PaginationMessages;
import me.whereareiam.intercept.type.ComponentType;

import java.util.List;
import java.util.Map;

/**
 * Configuration for message handling and fallback formatting.
 * Defines how missing translations should be displayed based on their source context.
 */
@Getter
@Setter
@ToString
public class Messages {
	/**
	 * Global prefix for all messages
	 */
	private String prefix;

	/**
	 * Command-related configuration
	 */
	private Commands commands;

	/**
	 * Fallback formatting configuration
	 */
	private Fallback fallback;

	/**
	 * Configuration for command-related messages and behavior.
	 */
	@Getter
	@Setter
	@ToString
	public static class Commands {
		/**
		 * Exception messages from Commandant
		 */
		private ExceptionMessages exceptions;

		/**
		 * Pagination configuration from Commandant
		 */
		private PaginationMessages pagination;

		/**
		 * Help command formatting configuration from Commandant
		 */
		private HelpMessages help;
		/**
		 * Custom argument display names (project-specific)
		 * Key: argument name (e.g., "page")
		 * Value: display name (e.g., "page number")
		 */
		private Map<String, String> arguments;

		/**
		 * Reload command messages
		 */
		private Reload reload;

		/**
		 * Inspect command messages
		 */
		private Inspect inspect;

		/**
		 * Configuration for reload command messages.
		 */
		@Getter
		@Setter
		@ToString
		public static class Reload {
			/**
			 * Success message when reload completes successfully.
			 * Placeholders:
			 * - {prefix}: The global message prefix
			 */
			private String success;

			/**
			 * Error message when reload fails.
			 * Placeholders:
			 * - {prefix}: The global message prefix
			 * - {error}: The error message
			 */
			private String error;
		}

		/**
		 * Configuration for inspect command messages.
		 */
		@Getter
		@Setter
		@ToString
		public static class Inspect {
			/**
			 * Message shown when inspection mode is enabled.
			 * Placeholders:
			 * - {prefix}: The global message prefix
			 */
			private String enabled;

			/**
			 * Message shown when inspection mode is disabled.
			 * Placeholders:
			 * - {prefix}: The global message prefix
			 */
			private String disabled;

			/**
			 * Configuration for hover text shown when hovering over messages in inspection mode.
			 */
			private Hover hover;

			/**
			 * Configuration for hover text in inspection mode.
			 */
			@Getter
			@Setter
			@ToString
			public static class Hover {
				/**
				 * Hover text format shown when hovering over messages in inspection mode.
				 * Each string in the list represents a line of the hover text.
				 * Placeholders:
				 * - {pattern}: The regex pattern (may be truncated if maxPatternLength is set)
				 * - {fullPattern}: The full regex pattern (always complete, even if truncated in display)
				 * - {truncated}: The truncation indicator if pattern was truncated, empty string otherwise
				 */
				private List<String> format;

				/**
				 * Text to append when the pattern is truncated.
				 * This value is used as the {truncated} placeholder in format.
				 */
				private String truncationFormat;

				/**
				 * Maximum number of characters to show in the hover text pattern.
				 * If the pattern is longer, it will be truncated and the truncation indicator appended.
				 * Set to 0 or negative to disable truncation.
				 */
				private int maxPatternLength;
			}
		}
	}

	/**
	 * Configuration for fallback behavior when translations are missing.
	 * Allows different formatting based on message source (chat, command, system, etc.)
	 */
	@Getter
	@Setter
	@ToString
	public static class Fallback {
		/**
		 * Whether to enable source-specific fallback formatting
		 */
		private boolean enabled;

		/**
		 * Whether to show warning to admins when fallback is used
		 */
		private boolean warnAdmins;

		/**
		 * Source-specific fallback formats
		 * Key: component type (CHAT, ACTION_BAR, etc.)
		 * Value: format configuration for that source
		 */
		private Map<ComponentType, SourceFormat> formats;

		/**
		 * Default fallback format when source is not specified or no specific format exists
		 */
		private SourceFormat defaultFormat;

		/**
		 * Format configuration for a specific message source.
		 */
		@Getter
		@Setter
		@ToString
		public static class SourceFormat {
			/**
			 * Whether to use custom formatting for this source
			 */
			private boolean enabled;

			/**
			 * Format template for missing translations.
			 * Placeholders:
			 * - {key}: The message key that was not found
			 * - {locale}: The requested locale
			 * - {source}: The source of the message
			 * <p>
			 * Example: "<red>[Missing: {key}]</red>"
			 * Example: "[{source}] {key}"
			 */
			private String format;

			/**
			 * Whether to log a missing translation for this source
			 */
			private boolean logMissing;
		}
	}
}
