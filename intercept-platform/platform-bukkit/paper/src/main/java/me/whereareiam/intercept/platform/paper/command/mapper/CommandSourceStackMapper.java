package me.whereareiam.intercept.platform.paper.command.mapper;

import com.google.inject.Singleton;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.whereareiam.intercept.platform.paper.actor.console.PaperInterceptBrigadierConsole;
import me.whereareiam.intercept.platform.paper.actor.console.PaperInterceptConsole;
import me.whereareiam.intercept.platform.paper.actor.player.PaperInterceptBrigadierPlayer;
import me.whereareiam.intercept.platform.paper.actor.player.PaperInterceptPlayer;
import me.whereareiam.keystone.Actor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

/**
 * Maps between Paper's CommandSourceStack (Brigadier) and Intercept's Actor for modern command system.
 * Used by PaperCommandManager for Paper 1.20.5+ with Brigadier support.
 * Stores original CommandSourceStack for full reverse mapping support.
 */
@Singleton
public class CommandSourceStackMapper implements SenderMapper<CommandSourceStack, Actor> {

	@Override
	public @NonNull Actor map(@NonNull CommandSourceStack source) {
		// Handle console sender
		if (source.getSender() instanceof ConsoleCommandSender consoleSender) {
			return new PaperInterceptBrigadierConsole(consoleSender, source);
		}

		// Handle Brigadier's NullCommandSender
		if (source.getSender().getClass().getName().equals("io.papermc.paper.brigadier.NullCommandSender")) {
			// NullCommandSender is used for command blocks and other non-actor sources
			// We treat it as console for compatibility
			ConsoleCommandSender console = (ConsoleCommandSender) source.getSender();
			return new PaperInterceptBrigadierConsole(console, source);
		}

		if (source.getSender() instanceof Player player) {
			return new PaperInterceptBrigadierPlayer(player, source);
		}

		throw new UnsupportedOperationException("Unsupported command source type: " + source.getSender().getClass().getName());
	}

	@Override
	public @NonNull CommandSourceStack reverse(@NonNull Actor actor) {
		// Only brigadier-context types are supported for reverse mapping
		if (actor instanceof PaperInterceptBrigadierPlayer brigadierPlayer) {
			return brigadierPlayer.getCommandSourceStack();
		}

		if (actor instanceof PaperInterceptBrigadierConsole brigadierConsole) {
			return brigadierConsole.getCommandSourceStack();
		}

		// If base types are passed, it means they were created outside command context
		if (actor instanceof PaperInterceptPlayer || actor instanceof PaperInterceptConsole) {
			throw new UnsupportedOperationException(
					"Cannot reverse map base Actor types to CommandSourceStack. " +
							"Base PaperInterceptPlayer/Console are for non-command contexts only. " +
							"Actor was not created by CommandSourceStackMapper."
			);
		}

		throw new UnsupportedOperationException("Cannot reverse map Actor to CommandSourceStack - unknown actor type: " + actor.getClass().getName());
	}
}

