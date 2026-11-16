package me.whereareiam.intercept.adapter.command;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.adapter.command.definition.Commands;
import me.whereareiam.intercept.adapter.command.definition.CommandsProvider;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;

/**
 * Binds Intercept's commands and {@code commands.yml}. The platform provides the Cloud command manager they are
 * registered on.
 */
public final class CommandConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Commands.class).toProvider(CommandsProvider.class);

		Multibinder<RuntimeLifecycle> lifecycles = Multibinder.newSetBinder(binder(), RuntimeLifecycle.class);
		lifecycles.addBinding().to(CommandsProvider.class);
		lifecycles.addBinding().to(InterceptCommands.class);
	}
}
