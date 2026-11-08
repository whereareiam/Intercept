package me.whereareiam.intercept.platform.paper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.whereareiam.intercept.command.CommandSource;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * A Paper command source: the player or console behind Brigadier's source stack.
 *
 * @param stack the source stack Paper passes to commands
 */
public record PaperCommandSource(@NotNull CommandSourceStack stack) implements CommandSource {
	@Override
	public @Nullable UUID uniqueId() {
		return sender() instanceof Player player ? player.getUniqueId() : null;
	}

	@Override
	public @NotNull String name() {
		return sender() instanceof Player player ? player.getName() : "console";
	}

	@Override
	public @NotNull Locale language() {
		return sender() instanceof Player player ? player.locale() : Locale.ENGLISH;
	}

	@Override
	public boolean hasPermission(@NotNull String permission) {
		return sender().hasPermission(permission);
	}

	@Override
	public void send(@NotNull Component message) {
		sender().sendMessage(message);
	}

	private CommandSender sender() {
		return stack.getSender();
	}
}
