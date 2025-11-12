package me.whereareiam.intercept.platform.common.interceptor.packetevents;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface for packet processors that can handle multiple packet types.
 * Processors declare which packet types they handle and provide handlers for each.
 */
public interface PacketProcessor {
	/**
	 * Gets the packet handlers provided by this processor.
	 * Each entry maps a packet type to its handler function.
	 *
	 * @return Map of packet types to their handlers
	 */
	Map<PacketTypeCommon, Consumer<PacketSendEvent>> getPacketHandlers();
}