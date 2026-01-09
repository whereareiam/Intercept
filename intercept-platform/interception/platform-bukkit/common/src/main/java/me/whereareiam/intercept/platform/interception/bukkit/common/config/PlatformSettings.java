package me.whereareiam.intercept.platform.interception.bukkit.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.model.Event;

import java.util.Locale;
import java.util.Map;

/**
 * Platform-specific configuration settings for Bukkit.
 * Contains settings that may vary between different server platforms.
 */
@Getter
@Setter
@ToString
public class PlatformSettings {
	/**
	 * Default locale for messages.
	 */
	private Locale locale;

	/**
	 * Serialization configuration.
	 */
	private Serialization serialization;

	/**
	 * Event listener configurations.
	 */
	private Listeners listeners;

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

	/**
	 * Configuration for event listeners.
	 * Maps event names to their corresponding event configurations.
	 */
	@Getter
	@Setter
	@ToString
	public static class Listeners {
		/**
		 * Map of event name to event configuration.
		 */
		private Map<String, Event> events;
	}
}
