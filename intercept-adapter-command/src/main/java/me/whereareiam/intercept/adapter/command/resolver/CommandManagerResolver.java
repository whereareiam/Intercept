package me.whereareiam.intercept.adapter.command.resolver;

import org.incendo.cloud.CommandManager;

/**
 * Strategy interface for resolving CommandManager instances.
 * Allows different platforms to provide CommandManagers in different ways:
 * - Interception platforms (Paper/Velocity) create new managers via providers
 * - Direct platforms (Oraylen/Minestom) use existing platform-provided managers
 *
 * @param <C> the command sender type
 */
public interface CommandManagerResolver<C> {
	/**
	 * Resolves and returns a CommandManager instance.
	 *
	 * @return the resolved CommandManager
	 */
	CommandManager<C> resolve();
}
