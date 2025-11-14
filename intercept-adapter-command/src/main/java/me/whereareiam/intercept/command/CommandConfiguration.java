package me.whereareiam.intercept.command;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import me.whereareiam.commandant.CommandRegistrar;
import me.whereareiam.keystone.Actor;

/**
 * Guice configuration module for command adapter.
 * Binds command-related services.
 */
public class CommandConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(new TypeLiteral<CommandRegistrar<Actor>>() {}).toProvider(CommandRegistrarProvider.class);
		bind(CommandService.class).to(DefaultCommandService.class);
	}
}