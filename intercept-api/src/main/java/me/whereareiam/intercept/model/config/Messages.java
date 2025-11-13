package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.commandant.model.ExceptionMessages;
import me.whereareiam.intercept.type.ComponentType;

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
