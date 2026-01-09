package me.whereareiam.intercept.platform.interception.bukkit.common.interceptor.packetevents.actionbar;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.interception.actionbar.ActionBarInterceptionContext;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptor;
import me.whereareiam.intercept.platform.interception.bukkit.common.interceptor.packetevents.PacketProcessor;
import me.whereareiam.intercept.platform.interception.bukkit.common.interceptor.packetevents.PacketEventsPacketRouter;
import me.whereareiam.intercept.util.LocaleUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * PacketEvents-based implementation of action bar interceptor.
 * Processes action bar packets (ACTION_BAR and SYSTEM_CHAT_MESSAGE with overlay=true).
 * <p>
 * This processor does NOT register its own PacketEvents listener.
 * Instead, it is called by {@link PacketEventsPacketRouter}.
 */
@RequiredArgsConstructor
public class PacketEventsActionBarInterceptionProcessor implements ActionBarInterceptor, PacketProcessor {
	private final ActionBarInterceptionProcessor processor;

	@Override
	public Map<PacketTypeCommon, Consumer<PacketSendEvent>> getPacketHandlers() {
		return Map.of(
				PacketType.Play.Server.ACTION_BAR, this::processActionBarPacket,
				PacketType.Play.Server.SYSTEM_CHAT_MESSAGE, this::processSystemChatMessagePacket
		);
	}

	/**
	 * Processes an ACTION_BAR packet (dedicated action bar packet).
	 * Called by the packet router.
	 *
	 * @param event The packet send event
	 */
	private void processActionBarPacket(PacketSendEvent event) {
		WrapperPlayServerActionBar packet = new WrapperPlayServerActionBar(event);
		Player player = event.getPlayer();
		Component message = packet.getActionBarText();

		Locale locale = LocaleUtil.parseLocale(player.getLocale());

		ActionBarInterceptionContext context = new ActionBarInterceptionContext(
				player.getUniqueId(),
				locale,
				message
		);

		Component processedMessage = processor.processActionBar(context);

		if (processedMessage != null && !processedMessage.equals(message)) {
			packet.setActionBarText(processedMessage);
			event.markForReEncode(true);
		}
	}

	/**
	 * Processes a SYSTEM_CHAT_MESSAGE packet with overlay=true (action bar via system chat).
	 * Called by the packet router.
	 *
	 * @param event The packet send event
	 */
	private void processSystemChatMessagePacket(PacketSendEvent event) {
		WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(event);

		// Only process if it's an overlay (regular chat is handled separately)
		if (!packet.isOverlay()) {
			return;
		}

		Player player = event.getPlayer();
		Component message = packet.getMessage();

		Locale locale = LocaleUtil.parseLocale(player.getLocale());

		ActionBarInterceptionContext context = new ActionBarInterceptionContext(
				player.getUniqueId(),
				locale,
				message
		);

		Component processedMessage = processor.processActionBar(context);

		if (processedMessage != null && !processedMessage.equals(message)) {
			packet.setMessage(processedMessage);
			event.markForReEncode(true);
		}
	}

	@Override
	public void shutdown() {
		// No-op: listener lifecycle is managed by the router
	}
}

