package me.whereareiam.intercept.adapter.command;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.adapter.command.definition.Commands;
import me.whereareiam.intercept.lifecycle.Lifecycle;

import java.nio.file.Path;

/**
 * Binds Intercept's commands. The platform provides the Cloud command manager they are registered on.
 */
public final class CommandConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(InterceptCommands.class);
	}

	@Provides
	@Singleton
	Commands commands(Configura configura, @Named("dataPath") Path dataPath) {
		return configura.update(dataPath.resolve("commands.yml"), Commands.class);
	}
}
