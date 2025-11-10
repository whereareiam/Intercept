package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.type.message.MessageSource;

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
	 * Fallback formatting configuration
	 */
	private Fallback fallback;

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
		 * Key: source name (CHAT, COMMAND, SYSTEM, etc.)
		 * Value: format configuration for that source
		 */
		private Map<MessageSource, SourceFormat> formats;

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
