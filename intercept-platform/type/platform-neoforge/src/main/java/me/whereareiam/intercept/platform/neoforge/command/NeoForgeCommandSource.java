package me.whereareiam.intercept.platform.neoforge.command;

import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.interception.library.mixin.VanillaText;
import me.whereareiam.semantica.format.adventure.MinecraftLocales;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * A NeoForge command source: a player, the console or a command block.
 *
 * @param stack the source the game passes to commands
 */
public record NeoForgeCommandSource(@NotNull CommandSourceStack stack) implements CommandSource {
	// Operators of this level may run every command; Intercept's commands ask for it when they name a permission.
	private static final int OPERATOR = 2;

	@Override
	public @Nullable UUID uniqueId() {
		ServerPlayer player = stack.getPlayer();
		return player == null ? null : player.getUUID();
	}

	@Override
	public @NotNull String name() {
		ServerPlayer player = stack.getPlayer();
		return player == null ? "console" : player.getGameProfile().getName();
	}

	@Override
	public @NotNull Locale language() {
		ServerPlayer player = stack.getPlayer();
		return player == null ? Locale.ENGLISH : MinecraftLocales.toLocale(player.clientInformation().language());
	}

	@Override
	public boolean hasPermission(@NotNull String permission) {
		return permission.isEmpty() || stack.hasPermission(OPERATOR);
	}

	@Override
	public void send(@NotNull Component message) {
		stack.sendSystemMessage(VanillaText.vanilla(message, stack.registryAccess()));
	}
}
