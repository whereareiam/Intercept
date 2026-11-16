package me.whereareiam.intercept.interception.library.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Registers Intercept's packet listener with PacketEvents while Intercept runs.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PacketEventsInterception implements RuntimeLifecycle {
	private final @NotNull MessagePacketListener listener;
	private PacketListenerCommon registered;

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.INTERCEPTION;
	}

	@Override
	public void initialize() {
		registered = PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.HIGH);
	}

	@Override
	public void shutdown() {
		if (registered != null) PacketEvents.getAPI().getEventManager().unregisterListener(registered);
		registered = null;
	}
}
