package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.dialectica.type.DatabaseType;

/**
 * Persistence configuration for player data storage.
 * Supports PostgreSQL and MariaDB databases.
 */
@Getter
@Setter
@ToString
public class Persistence {
	/**
	 * Whether database support is enabled.
	 * When disabled, database dependencies will not be loaded.
	 * Default: false
	 */
	private boolean enabled;

	/**
	 * Database type to use.
	 */
	private DatabaseType type;

	/**
	 * Persistence server hostname or IP address.
	 */
	private String host;

	/**
	 * Persistence server port.
	 */
	private int port;

	/**
	 * Persistence name to connect to.
	 */
	private String database;

	/**
	 * Persistence username for authentication.
	 */
	private String username;

	/**
	 * Persistence password for authentication.
	 */
	private String password;

	/**
	 * When true, download all messages from the database into storage on startup.
	 * Default: false
	 */
	private boolean autoDownloadOnStartup;

	/**
	 * HikariCP connection pool configuration.
	 */
	private Hikari hikari;

	/**
	 * HikariCP connection pool configuration.
	 * Provides recommended settings for connection pooling.
	 */
	@Getter
	@Setter
	@ToString
	public static class Hikari {
		/**
		 * Pool name (client name) for identifying connections in database logs.
		 */
		private String poolName;

		/**
		 * Maximum number of connections in the pool.
		 */
		private int maximumPoolSize;

		/**
		 * Minimum number of idle connections to maintain.
		 */
		private int minimumIdle;

		/**
		 * Maximum number of milliseconds to wait for a connection from the pool.
		 */
		private long connectionTimeout;

		/**
		 * Maximum amount of time (in milliseconds) that a connection can sit idle in the pool.
		 */
		private long idleTimeout;

		/**
		 * Maximum lifetime (in milliseconds) of a connection in the pool.
		 */
		private long maxLifetime;
	}
}