package me.whereareiam.intercept.platform.direct.oraylen.actor.player;

import me.whereareiam.intercept.model.player.InterceptPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.oraylen.api.model.actor.Echo;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Oraylen-specific Intercept player wrapper.
 */
public final class OraylenInterceptPlayer extends InterceptPlayer {
	private final Echo echo;

	public OraylenInterceptPlayer(@NotNull Echo echo) {
		super(echo.getUniqueId(), echo.getUsername());
		this.echo = echo;
	}

	@Override
	public @NotNull Locale getClientLocale() {
		return echo.getLocale();
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		echo.sendMessage(message);
	}

	@Override
	public boolean hasPermission(@NotNull String permission) {
		return echo.hasPermission(permission);
	}

	@Override
	public @NotNull Audience getAudience() {
		return echo.getAudience();
	}
}
