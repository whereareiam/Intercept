package me.whereareiam.intercept.lifecycle;

import org.jetbrains.annotations.NotNull;

/**
 * A part of Intercept that starts with it and stops with it. Parts start phase by phase, in the order of
 * {@link Phase}, and stop in the reverse order.
 */
public interface Lifecycle {
	/**
	 * Returns the phase this part starts in.
	 *
	 * @return the phase
	 */
	@NotNull Phase phase();

	/**
	 * Starts this part. A failure stops the parts already started and Intercept does not start.
	 *
	 * @throws Exception when the part cannot start
	 */
	void start() throws Exception;

	/**
	 * Stops this part; called only when {@link #start()} succeeded.
	 */
	void stop();

	/**
	 * The phases parts start in.
	 */
	enum Phase {
		/**
		 * The database.
		 */
		PERSISTENCE,
		/**
		 * The connection to the other instances.
		 */
		REPLICATION,
		/**
		 * The translation catalog.
		 */
		CATALOG,
		/**
		 * Shared messages, languages, interception and commands.
		 */
		RUNTIME
	}
}
