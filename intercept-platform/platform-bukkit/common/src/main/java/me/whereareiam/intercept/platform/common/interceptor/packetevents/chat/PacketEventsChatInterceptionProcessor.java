package me.whereareiam.intercept.platform.common.interceptor.packetevents.chat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptor;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PacketEvents-based implementation of chat interceptor.
 */
public class PacketEventsChatInterceptionProcessor implements ChatInterceptor {
	private final ChatInterceptionProcessor processor;
	private final PacketListenerAbstract listener;

	public PacketEventsChatInterceptionProcessor(ChatInterceptionProcessor processor) {
		this.processor = processor;

		this.listener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
			@Override
			public void onPacketSend(@NotNull PacketSendEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE)
					handleSystemChatMessage(event);
			}
		};

		PacketEvents.getAPI().getEventManager().registerListener(listener);
	}

	private void handleSystemChatMessage(PacketSendEvent event) {
		WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(event);
		Player player = event.getPlayer();
		Component message = packet.getMessage();

		ChatInterceptionContext context = new ChatInterceptionContext(
				player.getUniqueId(),
				message
		);

		Component processedMessage = processor.processChat(context);

		if (processedMessage != null && !processedMessage.equals(message))
			packet.setMessage(processedMessage);
	}

	@Override
	public void shutdown() {
		PacketEvents.getAPI().getEventManager().unregisterListener(listener);
	}
}

