package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.commandant.model.message.ExceptionMessages;
import me.whereareiam.commandant.model.message.HelpMessages;
import me.whereareiam.commandant.model.message.PaginationMessages;

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
		 * Reload command messages
		 */
		private Reload reload;

		/**
		 * Inspect command messages
		 */
		private Inspect inspect;

		/**
		 * Locale command messages
		 */
		private LocaleCommand locale;

		/**
		 * Database command messages
		 */
		private Database database;

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
			 * - <prefix>: The global message prefix
			 */
			private String success;

			/**
			 * Error message when reload fails.
			 * Placeholders:
			 * - <prefix>: The global message prefix
			 * - <error>: The error message
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
			 * - <prefix>: The global message prefix
			 */
			private String enabled;

			/**
			 * Message shown when inspection mode is disabled.
			 * Placeholders:
			 * - <prefix>: The global message prefix
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
				 * - <pattern>: The regex pattern (may be truncated if maxPatternLength is set)
				 * - <fullPattern>: The full regex pattern (always complete, even if truncated in display)
				 * - <truncated>: The truncation indicator if pattern was truncated, empty string otherwise
				 */
				private List<String> format;

				/**
				 * Text to append when the pattern is truncated.
				 * This value is used as the <truncated> placeholder in format.
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

		/**
		 * Configuration for locale command messages.
		 */
		@Getter
		@Setter
		@ToString
		public static class LocaleCommand {
			/**
			 * Message shown when the executor is not a player.
			 */
			private String playerOnly;

			/**
			 * Message shown when a locale argument is missing.
			 */
			private String missingLocaleArgument;

			/**
			 * Message shown when target-specific arguments are missing.
			 */
			private String missingTargetArguments;

			/**
			 * Message shown when the supplied locale cannot be parsed.
			 */
			private String invalidLocale;

			/**
			 * Message shown when a target player cannot be found.
			 */
			private String playerNotFound;

			/**
			 * Message shown after a player updates their own locale.
			 */
			private String selfUpdated;

			/**
			 * Message shown after updating someone else's locale.
			 */
			private String targetUpdated;
		}

		@Getter
		@Setter
		@ToString
		public static class Database {
			/**
			 * Upload command messages
			 */
			private Upload upload;

			/**
			 * Download command messages
			 */
			private Download download;

			/**
			 * Configuration for database upload command messages.
			 */
			@Getter
			@Setter
			@ToString
			public static class Upload {
				/**
				 * Message shown when there are no messages to upload.
				 * Placeholders:
				 * - <prefix>: The global message prefix
				 */
				private String noMessages;

				/**
				 * Message shown when starting the upload process.
				 * Placeholders:
				 * - <prefix>: The global message prefix
				 * - <entries>: The number of entries being uploaded
				 * - <files>: The number of files being uploaded
				 */
				private String uploading;

				/**
				 * Success message when upload completes successfully.
				 * Placeholders:
				 * - <prefix>: The global message prefix
				 * - <entries>: The number of entries uploaded
				 */
				private String success;

				/**
				 * Error message when upload fails.
				 * Placeholders:
				 * - <prefix>: The global message prefix
				 * - <error>: The error message
				 */
				private String error;
			}

			/**
			 * Configuration for database download command messages.
			 */
			@Getter
			@Setter
			@ToString
			public static class Download {
				/**
				 * Message shown when starting the download process.
				 */
				private String preparing;

				/**
				 * Message shown when there are no entries/files in the database.
				 */
				private String noEntries;

				/**
				 * Success message when download completes successfully.
				 * Placeholders:
				 * - <files>: Number of files written
				 * - <entries>: Number of entries written
				 * - <time>: Total time in milliseconds
				 */
				private String success;

				/**
				 * Error message when download fails.
				 * Placeholders:
				 * - <error>: The error message/reason
				 */
				private String error;
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
		 * Source-specific fallback formats.
		 * Key: source identifier (e.g., "CHAT", "ACTION_BAR")
		 * Value: format configuration for that source
		 */
		private Map<String, SourceFormat> formats;

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
			 * - <key>: The message key that was not found
			 * - <locale>: The requested locale
			 * - <source>: The source of the message
			 * <p>
			 * Example: "<red>[Missing: <key>]</red>"
			 * Example: "[<source>] <key>"
			 */
			private String format;

			/**
			 * Whether to log a missing translation for this source
			 */
			private boolean logMissing;
		}
	}
}
