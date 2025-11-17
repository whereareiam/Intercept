package me.whereareiam.intercept.adapter.database.initializer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.DatabaseConfig;
import me.whereareiam.intercept.type.DatabaseType;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.SQLException;

/**
 * Initializes HikariCP connection pool.
 * This class is only loaded after database dependencies are available.
 */
public class HikariInitializer {

	/**
	 * Initializes HikariCP DataSource with the given configuration.
	 *
	 * @param databaseConfig the database configuration
	 * @return the initialized DataSource
	 */
	public static DataSource initialize(DatabaseConfig databaseConfig) {
		DatabaseType type = databaseConfig.getType() != null ? databaseConfig.getType() : DatabaseType.POSTGRES;
		DatabaseConfig.Hikari hikariConfig = databaseConfig.getHikari();

		// Build JDBC URL based on database type
		String jdbcUrl = buildJdbcUrl(type, databaseConfig);
		Logger.info("Connecting to database at %s", maskJdbcUrl(jdbcUrl));

		try {
			// Configure HikariCP DataSource
			HikariConfig hikariConfigObj = new HikariConfig();
			hikariConfigObj.setJdbcUrl(jdbcUrl);
			hikariConfigObj.setUsername(databaseConfig.getUsername());
			hikariConfigObj.setPassword(databaseConfig.getPassword());

			// Apply HikariCP pool settings from config
			hikariConfigObj.setPoolName(hikariConfig.getPoolName());
			hikariConfigObj.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
			hikariConfigObj.setMinimumIdle(hikariConfig.getMinimumIdle());
			hikariConfigObj.setConnectionTimeout(hikariConfig.getConnectionTimeout());
			hikariConfigObj.setIdleTimeout(hikariConfig.getIdleTimeout());
			hikariConfigObj.setMaxLifetime(hikariConfig.getMaxLifetime());

			// Disable fail-fast to allow plugin to start even if database is unavailable
			hikariConfigObj.setInitializationFailTimeout(-1);

			// Create HikariCP DataSource
			HikariDataSource dataSource = new HikariDataSource(hikariConfigObj);

			Logger.info("Database connection pool created (max: %d, min idle: %d)",
					hikariConfig.getMaximumPoolSize(),
					hikariConfig.getMinimumIdle());

			return dataSource;
		} catch (Exception e) {
			handleInitializationError(e, databaseConfig);
			throw new RuntimeException("Failed to initialize HikariCP", e);
		}
	}

	private static void handleInitializationError(Exception e, DatabaseConfig databaseConfig) {
		Throwable cause = e.getCause();
		String errorMessage = e.getMessage();

		// Check if it's a connection error
		boolean isConnectionError = e instanceof HikariPool.PoolInitializationException;

		if (!isConnectionError && cause != null) {
			if (cause instanceof ConnectException ||
					cause instanceof SQLException ||
					cause.getClass().getName().startsWith("org.postgresql.") ||
					cause.getClass().getName().startsWith("org.mariadb.")) {
				isConnectionError = true;
			}
		}

		if (isConnectionError) {
			Logger.severe("Failed to connect to database server. Please ensure:");
			Logger.severe("  - Database server is running");
			Logger.severe("  - Host and port are correct (%s:%d)", databaseConfig.getHost(), databaseConfig.getPort());
			Logger.severe("  - Database server is accepting connections");
			Logger.severe("Database features will be disabled until connection is established.");
			return;
		}

		Logger.severe("Failed to initialize database connection: %s", errorMessage);
		if (cause != null && !errorMessage.equals(cause.getMessage()))
			Logger.severe("Caused by: %s", cause.getMessage());
	}

	private static String buildJdbcUrl(DatabaseType type, DatabaseConfig databaseConfig) {
		String host = databaseConfig.getHost() + ":" + databaseConfig.getPort();
		String database = databaseConfig.getDatabase();

		return switch (type) {
			case POSTGRES -> String.format("jdbc:postgresql://%s/%s", host, database);
			case MARIADB -> String.format("jdbc:mariadb://%s/%s", host, database);
		};
	}

	private static String maskJdbcUrl(String jdbcUrl) {
		return jdbcUrl.replaceAll("password=[^;&]+", "password=***");
	}

	/**
	 * Shuts down the HikariCP DataSource.
	 *
	 * @param dataSource the DataSource to close
	 */
	public static void shutdown(DataSource dataSource) {
		if (dataSource == null) return;

		try {
			if (dataSource instanceof HikariDataSource hikariDataSource) {
				hikariDataSource.close();
				Logger.info("Database connection pool closed");
			}
		} catch (Exception e) {
			Logger.warn("Error closing connection pool: %s", e.getMessage());
		}
	}
}

