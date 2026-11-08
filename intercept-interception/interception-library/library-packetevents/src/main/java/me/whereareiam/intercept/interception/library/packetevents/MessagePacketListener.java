package me.whereareiam.intercept.interception.library.packetevents;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerDisconnect;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.interception.library.OutgoingMessages;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

/**
 * Translates outgoing system messages, action bars and disconnect reasons, and, where the platform asks for it,
 * reports a player's chosen language to the server in place of their client's.
 *
 * <p>Player chat travels in its own packet, signed by the sender, and is never touched.</p>
 */
@Singleton
public final class MessagePacketListener implements PacketListener {
	private final OutgoingMessages messages;
	private final ClientLocales locales;

	@Inject
	public MessagePacketListener(@NotNull OutgoingMessages messages, @NotNull ClientLocales locales) {
		this.messages = messages;
		this.locales = locales;
	}

	@Override
	public void onPacketSend(@NotNull PacketSendEvent event) {
		UUID receiver = event.getUser().getUUID();
		Locale client = locales.reported(receiver);

		if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
			WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(event);
			Surface surface = packet.isOverlay() ? Surface.ACTION_BAR : Surface.CHAT;
			Component rewritten = messages.rewrite(surface, packet.getMessage(), receiver, client);
			if (rewritten == null) return;

			packet.setMessage(rewritten);
			event.markForReEncode(true);
		} else if (event.getPacketType() == PacketType.Play.Server.ACTION_BAR) {
			WrapperPlayServerActionBar packet = new WrapperPlayServerActionBar(event);
			Component rewritten = messages.rewrite(Surface.ACTION_BAR, packet.getActionBarText(), receiver, client);
			if (rewritten == null) return;

			packet.setActionBarText(rewritten);
			event.markForReEncode(true);
		} else if (event.getPacketType() == PacketType.Play.Server.DISCONNECT) {
			WrapperPlayServerDisconnect packet = new WrapperPlayServerDisconnect(event);
			Component rewritten = messages.rewrite(Surface.KICK, packet.getReason(), receiver, client);
			if (rewritten == null) return;

			packet.setReason(rewritten);
			event.markForReEncode(true);
		} else if (event.getPacketType() == PacketType.Configuration.Server.DISCONNECT) {
			WrapperConfigServerDisconnect packet = new WrapperConfigServerDisconnect(event);
			Component rewritten = messages.rewrite(Surface.KICK, packet.getReason(), receiver, client);
			if (rewritten == null) return;

			packet.setReason(rewritten);
			event.markForReEncode(true);
		}
	}

	@Override
	public void onPacketReceive(@NotNull PacketReceiveEvent event) {
		UUID player = event.getUser().getUUID();
		if (player == null) return;

		if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
			WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);
			String reported = locales.report(player, packet);
			if (reported.equals(packet.getLocale())) return;

			packet.setLocale(reported);
			event.markForReEncode(true);
		} else if (event.getPacketType() == PacketType.Configuration.Client.CLIENT_SETTINGS) {
			WrapperConfigClientSettings packet = new WrapperConfigClientSettings(event);
			String reported = locales.report(player, packet.getLocale());
			if (reported.equals(packet.getLocale())) return;

			packet.setLocale(reported);
			event.markForReEncode(true);
		}
	}

	@Override
	public void onUserDisconnect(@NotNull UserDisconnectEvent event) {
		if (event.getUser().getUUID() != null) locales.forget(event.getUser().getUUID());
	}
}
