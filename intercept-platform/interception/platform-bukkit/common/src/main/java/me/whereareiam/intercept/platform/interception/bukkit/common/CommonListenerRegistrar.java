package me.whereareiam.intercept.platform.interception.bukkit.common;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.PlatformSettings;
import me.whereareiam.intercept.type.EventPriority;

@RequiredArgsConstructor
public abstract class CommonListenerRegistrar implements ListenerRegistrar {
	protected final Provider<PlatformSettings> settings;

	protected EventPriority determinePriority(Class<?> event) {
		PlatformSettings.Listeners listeners = settings.get().getListeners();

		if (listeners == null || listeners.getEvents() == null
				|| listeners.getEvents().isEmpty() || listeners.getEvents().get(event.getName()) == null)
			return EventPriority.NORMAL;

		EventPriority priority = listeners.getEvents().get(event.getName()).getPriority();
		if (priority == null) {
			Logger.warn("No priority found for event " + event.getName() + ", using default NORMAL.");
			return EventPriority.NORMAL;
		}

		return priority;
	}
}
