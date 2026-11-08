package me.whereareiam.intercept.interception.library.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.jetbrains.annotations.NotNull;

/**
 * Registers Intercept's packet listener with PacketEvents while Intercept runs.
 */
@Singleton
public final class PacketEventsInterception implements Lifecycle {
	private final MessagePacketListener listener;
	private PacketListenerCommon registered;

	@Inject
	public PacketEventsInterception(@NotNull MessagePacketListener listener) {
		this.listener = listener;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		registered = PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.HIGH);
	}

	@Override
	public void stop() {
		if (registered != null) PacketEvents.getAPI().getEventManager().unregisterListener(registered);
		registered = null;
	}
}
