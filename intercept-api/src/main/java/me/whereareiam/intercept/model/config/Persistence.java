package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.annotation.PostProcess;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.type.PersistenceType;

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
	 * Persistence type to use.
	 */
	private PersistenceType type;

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
	 * HikariCP connection pool configuration.
	 */
	private Hikari hikari;

	/**
	 * Table names configuration.
	 * Allows customizing table names for each entity.
	 */
	private Tables tables;

	@PostProcess
	public void updateDatabaseConstants() {
		Constants.Database.TYPE = type;
		Constants.Database.Tables.PLAYERS = tables.players;
		Constants.Database.Tables.MESSAGE_FILES = tables.messageFiles;
		Constants.Database.Tables.MESSAGE_ENTRIES = tables.messageEntries;
		Constants.Database.Tables.MESSAGE_TRANSLATIONS = tables.messageTranslations;
		Constants.Database.Tables.MESSAGE_REGEX_PATTERNS = tables.messageRegexPatterns;
		Constants.Database.Tables.MESSAGE_REGEX_PLACEHOLDERS = tables.messageRegexPlaceholders;
	}

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

	/**
	 * Table names configuration for database entities.
	 * Allows customizing table names for each entity type.
	 */
	@Getter
	@Setter
	@ToString
	public static class Tables {
		/**
		 * Table name for player entities.
		 */
		private String players;

		/**
		 * Table name for message file entities.
		 */
		private String messageFiles;

		/**
		 * Table name for message entry entities.
		 */
		private String messageEntries;

		/**
		 * Table name for message translation entities.
		 */
		private String messageTranslations;

		/**
		 * Table name for message regex pattern entities.
		 */
		private String messageRegexPatterns;

		/**
		 * Table name for message regex placeholder entities.
		 */
		private String messageRegexPlaceholders;
	}
}