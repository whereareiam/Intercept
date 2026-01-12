package me.whereareiam.intercept.adapter.command;

import com.google.inject.AbstractModule;
import me.whereareiam.intercept.command.CommandService;

/**
 * Guice configuration module for command adapter.
 * Binds command-related services.
 */
public class CommandConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(CommandService.class).to(DefaultCommandService.class);
	}
}
