package me.whereareiam.intercept.platform.direct.oraylen.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.Event;
import me.whereareiam.intercept.platform.direct.oraylen.config.PlatformSettings;
import me.whereareiam.intercept.type.EventPriority;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class PlatformSettingsTemplate implements TemplateProvider<PlatformSettings> {
	@Override
	public PlatformSettings supply(PlatformSettings settings) {
		configureListeners(settings);
		return settings;
	}

	private void configureListeners(PlatformSettings settings) {
		PlatformSettings.Listeners listeners = new PlatformSettings.Listeners();
		listeners.setEvents(defaultEvents());
		settings.setListeners(listeners);
	}

	private Map<String, Event> defaultEvents() {
		Map<String, Event> events = new HashMap<>();

		Event event = Event.builder().register(true).priority(EventPriority.LOWEST).build();
		events.put(PlayerDisconnectEvent.class.getName(), event);

		return events;
	}
}
