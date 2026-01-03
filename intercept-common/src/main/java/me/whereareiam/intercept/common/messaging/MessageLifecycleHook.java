package me.whereareiam.intercept.common.messaging;

/**
 * Hook for modules that need to react to message load/reload lifecycle.
 */
public interface MessageLifecycleHook {
	/**
	 * Called before messages are (re)loaded.
	 */
	default void beforeLoad() {}

	/**
	 * Called after messages are (re)loaded.
	 */
	default void afterLoad() {}
}
