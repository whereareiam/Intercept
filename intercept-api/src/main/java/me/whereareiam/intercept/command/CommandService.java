package me.whereareiam.intercept.command;

import me.whereareiam.commandant.Command;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

public interface CommandService {
	/**
	 * Registers a single command. If a root command is set, commands with "{command}" in usage
	 * will be registered as subcommands.
	 *
	 * @param command The command to register
	 */
	void register(@NotNull Command<Actor> command);
}