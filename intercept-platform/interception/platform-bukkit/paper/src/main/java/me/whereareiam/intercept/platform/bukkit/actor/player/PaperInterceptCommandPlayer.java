package me.whereareiam.intercept.platform.bukkit.actor.player;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-specific implementation of InterceptPlayer for legacy command contexts.
 * Wraps a Bukkit Player and stores the original CommandSender for reverse mapping.
 * Used by LegacyPaperCommandManager.
 */
@Getter
public class PaperInterceptCommandPlayer extends AbstractPaperInterceptPlayer {
	/**
	 * The original CommandSender used to create this actor
	 */
	@NotNull
	private final CommandSender commandSender;

	/**
	 * Creates a new PaperInterceptCommandPlayer wrapping a Bukkit actor with its CommandSender.
	 *
	 * @param bukkitPlayer  The Bukkit actor to wrap
	 * @param commandSender The original CommandSender
	 */
	public PaperInterceptCommandPlayer(@NotNull Player bukkitPlayer, @NotNull CommandSender commandSender) {
		super(bukkitPlayer);
		this.commandSender = commandSender;
	}
}