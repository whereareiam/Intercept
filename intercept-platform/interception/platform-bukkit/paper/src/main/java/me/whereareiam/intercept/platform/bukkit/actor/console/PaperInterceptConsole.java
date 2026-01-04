package me.whereareiam.intercept.platform.bukkit.actor.console;

import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-specific implementation of Console.
 * Wraps Bukkit ConsoleCommandSender for command execution.
 * This is the base variant without command context storage.
 */
public class PaperInterceptConsole extends AbstractPaperInterceptConsole {
	/**
	 * Creates a new PaperInterceptConsole wrapping a console sender.
	 *
	 * @param consoleSender The Bukkit console sender
	 */
	public PaperInterceptConsole(@NotNull ConsoleCommandSender consoleSender) {
		super(consoleSender);
	}
}