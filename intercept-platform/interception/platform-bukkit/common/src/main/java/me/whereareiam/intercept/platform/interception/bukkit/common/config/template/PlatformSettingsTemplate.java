package me.whereareiam.intercept.platform.interception.bukkit.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.Event;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.PlatformSettings;
import me.whereareiam.intercept.type.EventPriority;
import me.whereareiam.intercept.type.PlatformType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Template provider for platform settings.
 * Provides default configuration for platform-specific settings.
 */
@Singleton
public class PlatformSettingsTemplate implements TemplateProvider<PlatformSettings> {
	@Override
	public PlatformSettings supply(PlatformSettings settings) {
		// Set defaults
		settings.setLocale(Locale.US);

		PlatformSettings.Serialization serialization = new PlatformSettings.Serialization();
		serialization.setType("MINIMESSAGE");
		serialization.setEnableLegacyColors(false);
		settings.setSerialization(serialization);

		configureListeners(settings);

		return settings;
	}

	private void configureListeners(PlatformSettings settings) {
		PlatformSettings.Listeners listeners = new PlatformSettings.Listeners();
		if (PlatformType.isAtLeast(PlatformType.BUKKIT))
			listeners.setEvents(getPrioritiesForBukkit());

		settings.setListeners(listeners);
	}

	private Map<String, Event> getPrioritiesForBukkit() {
		Map<String, Event> priorities = new HashMap<>();

		Event event = Event.builder().register(true).priority(EventPriority.LOWEST).build();
		priorities.put("org.bukkit.event.player.PlayerQuitEvent", event);

		return priorities;
	}
}
