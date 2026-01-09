package me.whereareiam.intercept.adapter.command.resolver;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * CommandManagerResolver implementation that extracts an existing CommandManager from a platform's injector.
 * Used by direct platforms where the platform already provides its own
 * CommandManager with full integration to the native command system.
 *
 * <p>This resolver extracts the CommandManager from the platform's Guice injector,
 * allowing the platform module to remain agnostic of Cloud types while Intercept
 * can still access the platform's internal command infrastructure.</p>
 *
 * @param <C> the command sender type
 */
public class ExternalCommandManagerResolver<C> implements CommandManagerResolver<C> {
	private final Injector platformInjector;
	private final TypeLiteral<CommandManager<C>> commandManagerType = new TypeLiteral<>() {};

	@Inject
	public ExternalCommandManagerResolver(
			@Named("platformInjector") @NotNull Injector platformInjector
	) {
		this.platformInjector = platformInjector;
	}

	@Override
	public CommandManager<C> resolve() {
		return platformInjector.getInstance(Key.get(commandManagerType));
	}
}
