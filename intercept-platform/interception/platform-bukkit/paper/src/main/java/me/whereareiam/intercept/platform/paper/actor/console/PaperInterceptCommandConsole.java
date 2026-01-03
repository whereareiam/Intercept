package me.whereareiam.intercept.platform.paper.actor.console;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-specific implementation of Console for legacy command contexts.
 * Wraps Bukkit ConsoleCommandSender and stores the original CommandSender for reverse mapping.
 * Used by LegacyPaperCommandManager.
 */
@Getter
public class PaperInterceptCommandConsole extends AbstractPaperInterceptConsole {
	/**
	 * The original CommandSender used to create this console
	 */
	@NotNull
	private final CommandSender commandSender;

	/**
	 * Creates a new PaperInterceptCommandConsole wrapping a console sender with its CommandSender.
	 *
	 * @param consoleSender The Bukkit console sender
	 * @param commandSender The original CommandSender
	 */
	public PaperInterceptCommandConsole(@NotNull ConsoleCommandSender consoleSender, @NotNull CommandSender commandSender) {
		super(consoleSender);
		this.commandSender = commandSender;
	}
}