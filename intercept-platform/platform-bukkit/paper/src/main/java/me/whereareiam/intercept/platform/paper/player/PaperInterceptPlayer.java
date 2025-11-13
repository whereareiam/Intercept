package me.whereareiam.intercept.platform.paper.player;

import lombok.Getter;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-specific implementation of InterceptPlayer.
 * Wraps a Bukkit Player and provides direct access to platform-specific functionality.
 */
@Getter
public class PaperInterceptPlayer extends InterceptPlayer {
	/**
	 * The underlying Bukkit player instance
	 */
	@NotNull
	private final Player bukkitPlayer;

	/**
	 * Creates a new PaperInterceptPlayer wrapping a Bukkit player.
	 *
	 * @param bukkitPlayer The Bukkit player to wrap
	 */
	public PaperInterceptPlayer(@NotNull Player bukkitPlayer) {
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

	/**
	 * Gets the underlying Bukkit player for platform-specific operations.
	 *
	 * @return The Bukkit player instance
	 */
	@NotNull
	public Player getBukkitPlayer() {
		return bukkitPlayer;
	}
}