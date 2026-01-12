package me.whereareiam.intercept.platform.direct.oraylen.listener;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.Event;
import me.whereareiam.intercept.platform.direct.oraylen.config.PlatformSettings;
import me.whereareiam.intercept.platform.direct.oraylen.listener.connection.PlayerDisconnectListener;
import me.whereareiam.intercept.type.EventPriority;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class OraylenListenerRegistrar implements ListenerRegistrar {
	private final Injector injector;
	private final Provider<PlatformSettings> settingsProvider;
	private final Map<String, EventNode<? extends net.minestom.server.event.Event>> nodes = new ConcurrentHashMap<>();

	@Inject
	public OraylenListenerRegistrar(
			@NotNull Injector injector,
			@NotNull Provider<PlatformSettings> settingsProvider
	) {
		this.injector = injector;
		this.settingsProvider = settingsProvider;
	}

	@Override
	public void registerListeners() {
		registerListener(PlayerDisconnectEvent.class, injector.getInstance(PlayerDisconnectListener.class));
	}

	@Override
	public <T> void registerListener(@NotNull Class<T> eventClass, @NotNull DynamicListener<T> listener) {
		if (!net.minestom.server.event.Event.class.isAssignableFrom(eventClass)) {
			Logger.warn("Skipping listener registration for non-Minestom event: {}", eventClass.getName());
			return;
		}

		PlatformSettings.Listeners listeners = settingsProvider.get().getListeners();
		if (listeners == null || listeners.getEvents() == null) return;

		Event eventConfig = listeners.getEvents().get(eventClass.getName());
		if (eventConfig == null || !eventConfig.isRegister()) return;

		EventPriority priority = eventConfig.getPriority();
		EventNode<? extends net.minestom.server.event.Event> node = getOrCreateNode(eventClass, priority);

		@SuppressWarnings("unchecked")
		EventNode<net.minestom.server.event.Event> typedNode =
				(EventNode<net.minestom.server.event.Event>) node;
		typedNode.addListener(
				(Class<net.minestom.server.event.Event>) eventClass,
				event -> listener.onEvent(eventClass.cast(event))
		);
	}

	private <T> EventNode<? extends net.minestom.server.event.Event> getOrCreateNode(
			@NotNull Class<T> eventClass,
			EventPriority priority
	) {
		String key = eventClass.getName() + ":" + mapPriority(priority);

		return nodes.computeIfAbsent(key, _ -> {
			@SuppressWarnings("unchecked")
			Class<? extends net.minestom.server.event.Event> typedEvent =
					(Class<? extends net.minestom.server.event.Event>) eventClass;
			EventFilter<? extends net.minestom.server.event.Event, ?> filter =
					EventFilter.from(typedEvent, null, null);
			EventNode<? extends net.minestom.server.event.Event> node =
					EventNode.type("intercept-" + eventClass.getSimpleName(), filter)
							.setPriority(mapPriority(priority));

			MinecraftServer.getGlobalEventHandler().addChild(node);
			Logger.debug("Registering listener for event " + eventClass.getName());

			return node;
		});
	}

	private int mapPriority(EventPriority priority) {
		if (priority == null) return 2;

		return switch (priority) {
			case LOWEST -> 0;
			case LOW -> 1;
			case NORMAL -> 2;
			case HIGH -> 3;
			case HIGHEST -> 4;
		};
	}
}
