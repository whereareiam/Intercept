package me.whereareiam.intercept.interception.library.mixin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.interception.library.OutgoingMessages;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.semantica.format.adventure.MinecraftLocales;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Translates what a NeoForge server sends: the game sends keys unrendered, so each system message, action bar and
 * disconnect reason is converted to Adventure, rendered through the global translator in the player's language,
 * given to the interceptor, and converted back. A message nothing changed is sent as it was, so the client renders
 * the game's own keys itself.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PacketTranslation implements Lifecycle {
	private static volatile @Nullable PacketTranslation active;

	private final @NotNull OutgoingMessages messages;

	/**
	 * Rewrites a packet on its way to a player; called by the Mixin for every packet.
	 *
	 * @param listener the connection
	 * @param packet   the packet
	 * @return the packet to send
	 */
	public static @NotNull Packet<?> rewrite(@NotNull ServerCommonPacketListenerImpl listener, @NotNull Packet<?> packet) {
		PacketTranslation current = active;
		if (current == null || !(listener instanceof ServerGamePacketListenerImpl game)) return packet;

		ServerPlayer player = game.player;
		return switch (packet) {
			case ClientboundSystemChatPacket chat -> {
				Surface surface = chat.overlay() ? Surface.ACTION_BAR : Surface.CHAT;
				net.minecraft.network.chat.Component content = current.translate(surface, chat.content(), player);
				yield content == null ? packet : new ClientboundSystemChatPacket(content, chat.overlay());
			}
			case ClientboundSetActionBarTextPacket bar -> {
				net.minecraft.network.chat.Component text = current.translate(Surface.ACTION_BAR, bar.text(), player);
				yield text == null ? packet : new ClientboundSetActionBarTextPacket(text);
			}
			case ClientboundDisconnectPacket disconnect -> {
				net.minecraft.network.chat.Component reason = current.translate(Surface.KICK, disconnect.reason(), player);
				yield reason == null ? packet : new ClientboundDisconnectPacket(reason);
			}
			default -> packet;
		};
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		active = this;
	}

	@Override
	public void stop() {
		active = null;
	}

	private net.minecraft.network.chat.@Nullable Component translate(
			Surface surface,
			net.minecraft.network.chat.Component content,
			ServerPlayer player
	) {
		UUID receiver = player.getUUID();
		Locale client = MinecraftLocales.toLocale(player.clientInformation().language());
		Locale language = messages.language(receiver, client);

		Component original = VanillaText.adventure(content, player.registryAccess());
		Component rendered = GlobalTranslator.render(original, language);
		Component intercepted = messages.rewrite(surface, rendered, receiver, client);
		Component result = intercepted != null ? intercepted : rendered;
		if (result.equals(original)) return null;

		return VanillaText.vanilla(result, player.registryAccess());
	}
}
