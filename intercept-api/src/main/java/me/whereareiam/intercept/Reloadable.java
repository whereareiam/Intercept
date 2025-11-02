package me.whereareiam.intercept;

/**
 * Marker interface for components that can be reloaded.
 */
public interface Reloadable {
	/**
	 * Reloads the component's configuration or state.
	 */
	void reload();
}