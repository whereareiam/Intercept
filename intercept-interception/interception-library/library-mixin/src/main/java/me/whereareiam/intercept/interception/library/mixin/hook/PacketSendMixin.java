package me.whereareiam.intercept.interception.library.mixin.hook;

import me.whereareiam.intercept.interception.library.mixin.PacketTranslation;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hands every packet the server sends to a player to {@link PacketTranslation} before it is written.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class PacketSendMixin {
	@ModifyVariable(
			method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private Packet<?> intercept$translate(Packet<?> packet, Packet<?> original, @Nullable PacketSendListener listener) {
		return PacketTranslation.rewrite((ServerCommonPacketListenerImpl) (Object) this, packet);
	}
}
