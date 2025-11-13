package me.whereareiam.intercept.platform.paper.player;

import lombok.Getter;
import me.whereareiam.commandant.model.Console;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Paper-specific implementation of Console.
 * Wraps Bukkit ConsoleCommandSender for command execution.
 */
@Getter
public class PaperInterceptConsole implements Console {
	/**
	 * The underlying Bukkit console sender
	 */
	@NotNull
	private final ConsoleCommandSender consoleSender;

	/**
	 * Creates a new PaperInterceptConsole wrapping a console sender.
	 *
	 * @param consoleSender The Bukkit console sender
	 */
	public PaperInterceptConsole(@NotNull ConsoleCommandSender consoleSender) {
		this.consoleSender = consoleSender;
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		consoleSender.sendMessage(message);
	}

	@Override
	@NotNull
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	@NotNull
	public Audience getAudience() {
		return consoleSender;
	}

	/**
	 * Gets the underlying console sender for platform-specific operations.
	 *
	 * @return The console sender instance
	 */
	@NotNull
	public ConsoleCommandSender getConsoleSender() {
		return consoleSender;
	}
}
