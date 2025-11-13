package me.whereareiam.intercept.platform.paper.command.mapper;

import com.google.inject.Singleton;
import me.whereareiam.intercept.platform.paper.actor.console.PaperInterceptCommandConsole;
import me.whereareiam.intercept.platform.paper.actor.console.PaperInterceptConsole;
import me.whereareiam.intercept.platform.paper.actor.player.PaperInterceptCommandPlayer;
import me.whereareiam.intercept.platform.paper.actor.player.PaperInterceptPlayer;
import me.whereareiam.keystone.model.Actor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

/**
 * Maps between Bukkit's CommandSender and Intercept's Actor for legacy command system.
 * Used by LegacyPaperCommandManager for pre-1.20.5 or non-Brigadier setups.
 * Stores original CommandSender for full reverse mapping support.
 */
@Singleton
public class CommandSenderMapper implements SenderMapper<CommandSender, Actor> {

	@Override
	public @NonNull Actor map(@NonNull CommandSender source) {
		if (source instanceof ConsoleCommandSender consoleSender) {
			return new PaperInterceptCommandConsole(consoleSender, source);
		}

		if (source instanceof Player player) {
			return new PaperInterceptCommandPlayer(player, source);
		}

		throw new UnsupportedOperationException("Unsupported command sender type: " + source.getClass().getName());
	}

	@Override
	public @NonNull CommandSender reverse(@NonNull Actor actor) {
		// Only command-context types are supported for reverse mapping
		if (actor instanceof PaperInterceptCommandPlayer commandPlayer) {
			return commandPlayer.getCommandSender();
		}

		if (actor instanceof PaperInterceptCommandConsole commandConsole) {
			return commandConsole.getCommandSender();
		}

		// If base types are passed, it means they were created outside command context
		if (actor instanceof PaperInterceptPlayer || actor instanceof PaperInterceptConsole) {
			throw new UnsupportedOperationException(
					"Cannot reverse map base Actor types to CommandSender. " +
							"Base PaperInterceptPlayer/Console are for non-command contexts only. " +
							"Actor was not created by CommandSenderMapper."
			);
		}

		throw new UnsupportedOperationException("Cannot reverse map Actor to CommandSender - unknown actor type: " + actor.getClass().getName());
	}
}

