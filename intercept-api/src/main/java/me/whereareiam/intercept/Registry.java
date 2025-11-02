package me.whereareiam.intercept;

import java.util.Collection;

/**
 * Generic registry interface for managing registered components.
 *
 * @param <T> The type of components this registry manages
 */
public interface Registry<T> {
	/**
	 * Registers a component.
	 *
	 * @param component The component to register
	 */
	void register(T component);

	/**
	 * Unregisters a component.
	 *
	 * @param component The component to unregister
	 */
	void unregister(T component);

	/**
	 * Gets all registered components.
	 *
	 * @return A collection of all registered components
	 */
	Collection<T> getAll();

	/**
	 * Clears all registered components.
	 */
	void clear();
}