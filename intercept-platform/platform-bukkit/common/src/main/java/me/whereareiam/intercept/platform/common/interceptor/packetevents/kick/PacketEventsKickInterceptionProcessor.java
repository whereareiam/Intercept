package me.whereareiam.intercept.platform.common.interceptor.packetevents.kick;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.interceptor.kick.KickInterceptor;
import me.whereareiam.intercept.model.interception.kick.KickInterceptionContext;
import me.whereareiam.intercept.platform.common.interceptor.packetevents.PacketProcessor;
import me.whereareiam.intercept.util.LocaleUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * PacketEvents-based implementation of kick interceptor.
 * Processes disconnect packets (DISCONNECT) to translate kick reasons.
 * <p>
 * This processor does NOT register its own PacketEvents listener.
 * Instead, it is called by {@link me.whereareiam.intercept.platform.common.interceptor.packetevents.PacketEventsPacketRouter}.
 */
@RequiredArgsConstructor
public class PacketEventsKickInterceptionProcessor implements KickInterceptor, PacketProcessor {
	private final KickInterceptionProcessor processor;

	@Override
	public Map<PacketTypeCommon, Consumer<PacketSendEvent>> getPacketHandlers() {
		return Map.of(
				PacketType.Play.Server.DISCONNECT, this::processDisconnectPacket
		);
	}

	/**
	 * Processes a DISCONNECT packet (kick/disconnect reason).
	 * Called by the packet router.
	 *
	 * @param event The packet send event
	 */
	private void processDisconnectPacket(PacketSendEvent event) {
		WrapperPlayServerDisconnect packet = new WrapperPlayServerDisconnect(event);
		Player player = event.getPlayer();
		Component reason = packet.getReason();

		Locale locale = LocaleUtil.parseLocale(player.getLocale());

		KickInterceptionContext context = new KickInterceptionContext(
				player.getUniqueId(),
				locale,
				reason
		);

		Component processedReason = processor.processKick(context);

		if (processedReason != null && !processedReason.equals(reason)) {
			packet.setReason(processedReason);
			event.markForReEncode(true);
		}
	}

	@Override
	public void shutdown() {
		// No-op: listener lifecycle is managed by the router
	}
}