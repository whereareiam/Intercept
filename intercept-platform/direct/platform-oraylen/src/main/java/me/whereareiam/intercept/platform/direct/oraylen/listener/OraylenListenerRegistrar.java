package me.whereareiam.intercept.platform.direct.oraylen.listener;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.direct.oraylen.listener.connection.EchoDisconnectListener;
import me.whereareiam.intercept.platform.direct.oraylen.listener.connection.EchoSpawnListener;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.oraylen.api.annotation.EventListener;
import net.oraylen.api.event.type.player.EchoDisconnectEvent;
import net.oraylen.api.event.type.player.EchoSpawnEvent;
import net.oraylen.api.util.Events;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class OraylenListenerRegistrar implements ListenerRegistrar {
	private final Injector injector;

	@Inject
	public OraylenListenerRegistrar(@NotNull Injector injector) {
		this.injector = injector;
	}

	@Override
	public void registerListeners() {
		registerListener(
				EchoDisconnectEvent.class,
				injector.getInstance(EchoDisconnectListener.class)
		);
		registerListener(
				EchoSpawnEvent.class,
				injector.getInstance(EchoSpawnListener.class)
		);
	}

	@Override
	public <T> void registerListener(@NotNull Class<T> eventClass, @NotNull DynamicListener<T> listener) {
		if (Event.class.isAssignableFrom(eventClass)) {
			registerMinestomListener(eventClass, listener);
			return;
		}

		registerOraylenListener(eventClass, listener);
	}

	private <T> void registerMinestomListener(
			@NotNull Class<T> eventClass,
			@NotNull DynamicListener<T> listener
	) {
		@SuppressWarnings("unchecked")
		Class<? extends Event> typedEvent = (Class<? extends Event>) eventClass;
		MinecraftServer.getGlobalEventHandler().addListener(
				typedEvent,
				event -> listener.onEvent(eventClass.cast(event))
		);
	}

	private <T> void registerOraylenListener(@NotNull Class<T> eventClass, @NotNull DynamicListener<T> listener) {
		Object bridge = new OraylenEventBridge<>(eventClass, listener);
		Events.bus().register(bridge);
		Logger.debug("Registering listener for Oraylen event " + eventClass.getName());
	}

	@RequiredArgsConstructor
	private static final class OraylenEventBridge<T> {
		private final Class<T> eventClass;
		private final DynamicListener<T> listener;

		@EventListener
		public void onEvent(Object event) {
			if (eventClass.isInstance(event))
				listener.onEvent(eventClass.cast(event));
		}
	}
}
