package me.whereareiam.intercept.platform.direct.oraylen.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.model.Event;

import java.util.Map;

/**
 * Platform-specific configuration settings for Oraylen.
 */
@Getter
@Setter
@ToString
public class PlatformSettings {
	/**
	 * Event listener configurations.
	 */
	private Listeners listeners;

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
