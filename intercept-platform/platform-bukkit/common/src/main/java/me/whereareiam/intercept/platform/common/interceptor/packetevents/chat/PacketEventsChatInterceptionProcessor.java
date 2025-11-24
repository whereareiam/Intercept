package me.whereareiam.intercept.platform.common.interceptor.packetevents.chat;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptor;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import me.whereareiam.intercept.platform.common.interceptor.packetevents.PacketProcessor;
import me.whereareiam.intercept.util.LocaleUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * PacketEvents-based implementation of chat interceptor.
 * Processes chat-related packets (CHAT_MESSAGE and SYSTEM_CHAT_MESSAGE with overlay=false).
 * <p>
 * This processor does NOT register its own PacketEvents listener.
 * Instead, it is called by {@link me.whereareiam.intercept.platform.common.interceptor.packetevents.PacketEventsPacketRouter}.
 */
@RequiredArgsConstructor
public class PacketEventsChatInterceptionProcessor implements ChatInterceptor, PacketProcessor {
	private final ChatInterceptionProcessor processor;

	@Override
	public Map<PacketTypeCommon, Consumer<PacketSendEvent>> getPacketHandlers() {
		return Map.of(
				PacketType.Play.Server.CHAT_MESSAGE, this::processChatMessagePacket,
				PacketType.Play.Server.SYSTEM_CHAT_MESSAGE, this::processSystemChatMessagePacket
		);
	}

	/**
	 * Processes a CHAT_MESSAGE packet (player chat messages).
	 * Called by the packet router.
	 *
	 * @param event The packet send event
	 */
	private void processChatMessagePacket(PacketSendEvent event) {
		WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
		Player player = event.getPlayer();
		Component message = packet.getMessage().getChatContent();

		Locale locale = LocaleUtil.parseLocale(player.getLocale());

		ChatInterceptionContext context = new ChatInterceptionContext(
				player.getUniqueId(),
				locale,
				message
		);

		Component processedMessage = processor.processChat(context);

		if (processedMessage != null && !processedMessage.equals(message)) {
			packet.getMessage().setChatContent(processedMessage);
			event.markForReEncode(true);
		}
	}

	/**
	 * Processes a SYSTEM_CHAT_MESSAGE packet with overlay=false (chat area messages).
	 * Called by the packet router.
	 *
	 * @param event The packet send event
	 */
	private void processSystemChatMessagePacket(PacketSendEvent event) {
		WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(event);

		// Only process if it's NOT an overlay (action bar is handled separately)
		if (packet.isOverlay()) {
			return;
		}

		Player player = event.getPlayer();
		Component message = packet.getMessage();

		Locale locale = LocaleUtil.parseLocale(player.getLocale());

		ChatInterceptionContext context = new ChatInterceptionContext(
				player.getUniqueId(),
				locale,
				message
		);

		Component processedMessage = processor.processChat(context);

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

