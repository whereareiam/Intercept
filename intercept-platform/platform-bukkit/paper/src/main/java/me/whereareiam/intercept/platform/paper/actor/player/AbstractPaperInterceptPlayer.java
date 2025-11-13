package me.whereareiam.intercept.platform.paper.actor.player;

import lombok.Getter;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all Paper InterceptPlayer implementations.
 * Provides common functionality for wrapping Bukkit Players.
 */
@Getter
public abstract class AbstractPaperInterceptPlayer extends InterceptPlayer {
	/**
	 * The underlying Bukkit actor instance
	 */
	@NotNull
	private final Player bukkitPlayer;

	/**
	 * Creates a new AbstractPaperInterceptPlayer wrapping a Bukkit actor.
	 *
	 * @param bukkitPlayer The Bukkit actor to wrap
	 */
	protected AbstractPaperInterceptPlayer(@NotNull Player bukkitPlayer) {
		super(
				bukkitPlayer.getUniqueId(),
				bukkitPlayer.getName(),
				bukkitPlayer.locale()
		);
		this.bukkitPlayer = bukkitPlayer;
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		bukkitPlayer.sendMessage(message);
	}

	@Override
	public boolean hasPermission(@NotNull String permission) {
		return bukkitPlayer.hasPermission(permission);
	}

	@Override
	@NotNull
	public Audience getAudience() {
		return bukkitPlayer;
	}
}

