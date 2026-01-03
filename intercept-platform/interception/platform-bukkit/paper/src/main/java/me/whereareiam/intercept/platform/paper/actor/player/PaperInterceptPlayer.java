package me.whereareiam.intercept.platform.paper.actor.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-specific implementation of InterceptPlayer.
 * Wraps a Bukkit Player and provides direct access to platform-specific functionality.
 * This is the base variant without command context storage.
 */
public class PaperInterceptPlayer extends AbstractPaperInterceptPlayer {
	/**
	 * Creates a new PaperInterceptPlayer wrapping a Bukkit actor.
	 *
	 * @param bukkitPlayer The Bukkit actor to wrap
	 */
	public PaperInterceptPlayer(@NotNull Player bukkitPlayer) {
		super(bukkitPlayer);
	}
}