package me.whereareiam.intercept.platform.common.interceptor.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Central packet router for PacketEvents-based interception.
 * Routes incoming packets to the appropriate processor using a handler map strategy.
 * <p>
 * This router implements a single PacketEvents listener that dispatches to multiple
 * component processors, preventing duplicate packet registrations and ensuring efficient
 * packet processing.
 * <p>
 * Only registers the listener when at least one processor is active.
 */
public class PacketEventsPacketRouter {
	private final Map<PacketTypeCommon, List<Consumer<PacketSendEvent>>> handlers = new HashMap<>();
	@Nullable
	private PacketListenerAbstract listener;

	/**
	 * Registers a packet processor with this router.
	 * Automatically sets up handlers for all packet types declared by the processor.
	 *
	 * @param processor The processor to register
	 */
	public void registerProcessor(PacketProcessor processor) {
		processor.getPacketHandlers().forEach((packetType, handler) -> handlers
				.computeIfAbsent(packetType, k -> new ArrayList<>())
				.add(handler));

		ensureListenerRegistered();
	}

	/**
	 * Ensures the PacketEvents listener is registered if we have any handlers.
	 * Only creates and registers the listener once.
	 */
	private void ensureListenerRegistered() {
		if (listener == null && !handlers.isEmpty()) {
			listener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
				@Override
				public void onPacketSend(@NotNull PacketSendEvent event) {
					List<Consumer<PacketSendEvent>> handlerList = handlers.get(event.getPacketType());
					if (handlerList != null) {
						for (Consumer<PacketSendEvent> handler : handlerList) {
							handler.accept(event);
						}
					}
				}
			};
			PacketEvents.getAPI().getEventManager().registerListener(listener);
		}
	}
}