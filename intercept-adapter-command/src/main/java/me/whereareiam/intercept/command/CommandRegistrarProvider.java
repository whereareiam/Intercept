package me.whereareiam.intercept.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import me.whereareiam.commandant.CommandRegistrar;
import me.whereareiam.commandant.Commandant;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * Guice Provider for CommandRegistrar instances.
 * Creates a CommandRegistrar configured for Actor type.
 * Returns a singleton instance.
 */
public class CommandRegistrarProvider implements Provider<CommandRegistrar<Actor>> {
	private final Provider<CommandManager<Actor>> commandManagerProvider;
	private CommandRegistrar<Actor> registrar;

	@Inject
	public CommandRegistrarProvider(@NotNull Provider<CommandManager<Actor>> commandManagerProvider) {
		this.commandManagerProvider = commandManagerProvider;
	}

	@Override
	@NotNull
	public CommandRegistrar<Actor> get() {
		if (registrar == null) {
			registrar = Commandant.createRegistrar(commandManagerProvider.get());
		}
		return registrar;
	}
}