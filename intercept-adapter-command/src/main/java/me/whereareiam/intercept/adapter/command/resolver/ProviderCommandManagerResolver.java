package me.whereareiam.intercept.adapter.command.resolver;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * CommandManagerResolver implementation that creates new CommandManager instances via a Provider.
 * Used by interception platforms (Paper/Velocity) where Intercept creates the CommandManager
 * using Cloud's built-in platform integrations.
 *
 * @param <C> the command sender type
 */
public class ProviderCommandManagerResolver<C> implements CommandManagerResolver<C> {
	private final Provider<CommandManager<C>> provider;

	@Inject
	public ProviderCommandManagerResolver(@NotNull Provider<CommandManager<C>> provider) {
		this.provider = provider;
	}

	@Override
	public CommandManager<C> resolve() {
		return provider.get();
	}
}
