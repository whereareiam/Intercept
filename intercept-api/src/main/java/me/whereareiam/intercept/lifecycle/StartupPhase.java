package me.whereareiam.intercept.lifecycle;

/**
 * The phases Intercept starts in, in the order they run. Every {@link RuntimeLifecycle} names the phase it belongs
 * to; Intercept starts all lifecycles of a phase before the first one of the next phase, and stops them phase by
 * phase in reverse.
 */
public enum StartupPhase {
	/**
	 * The configuration files are loaded: {@code settings.yml} and the file of every family, such as
	 * {@code interception.yml}.
	 */
	CONFIGURATION,
	/** The stores Intercept keeps its state in are opened: the database, and the connection to the other instances. */
	PERSISTENCE,
	/** The translation catalog is built and registered as the authority in Adventure's global translator. */
	CATALOG,
	/** The core starts: the shared messages and their folders, and players' languages. */
	RUNTIME,
	/** Intercept starts translating the messages it intercepts on their way to players. */
	INTERCEPTION,
	/** The integrations with other plugins start, such as PlaceholderAPI. */
	INTEGRATIONS,
	/** Intercept starts handling players and their commands. */
	CONNECTIONS
}
