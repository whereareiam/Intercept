package me.whereareiam.intercept.platform.velocity.command;

import com.velocitypowered.api.proxy.Player;
import me.whereareiam.intercept.command.CommandSource;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * A Velocity command source: a player or the proxy's console.
 *
 * @param source the source Velocity passes to commands
 */
public record VelocityCommandSource(@NotNull com.velocitypowered.api.command.CommandSource source) implements CommandSource {
	@Override
	public @Nullable UUID uniqueId() {
		return source instanceof Player player ? player.getUniqueId() : null;
	}

	@Override
	public @NotNull String name() {
		return source instanceof Player player ? player.getUsername() : "console";
	}

	@Override
	public @NotNull Locale language() {
		if (!(source instanceof Player player)) return Locale.ENGLISH;

		Locale reported = player.getPlayerSettings().getLocale();
		return reported == null ? Locale.ENGLISH : reported;
	}

	@Override
	public boolean hasPermission(@NotNull String permission) {
		return source.hasPermission(permission);
	}

	@Override
	public void send(@NotNull Component message) {
		source.sendMessage(message);
	}
}
