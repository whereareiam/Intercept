package me.whereareiam.intercept.adapter.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.type.PersistenceType;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.SQLException;

/**
 * Factory for creating HikariCP DataSource instances.
 */
public final class DataSourceFactory {
	/**
	 * Creates a HikariCP DataSource with the given configuration.
	 *
	 * @param persistence the database configuration
	 * @return the initialized DataSource
	 */
	public static DataSource create(Persistence persistence) {
		PersistenceType type = Constants.Database.TYPE;
		Persistence.Hikari hikariConfig = persistence.getHikari();

		// Build JDBC URL based on database type
		String jdbcUrl = JdbcUrlFactory.create(persistence);
		Logger.info("Connecting to database at %s", maskJdbcUrl(jdbcUrl));

		try {
			// Configure HikariCP DataSource
			HikariConfig hikariConfigObj = new HikariConfig();
			hikariConfigObj.setJdbcUrl(jdbcUrl);
			hikariConfigObj.setDriverClassName(getDriverClassName(type));
			hikariConfigObj.setUsername(persistence.getUsername());
			hikariConfigObj.setPassword(persistence.getPassword());

			// Apply HikariCP pool settings from config
			hikariConfigObj.setPoolName(hikariConfig.getPoolName());
			hikariConfigObj.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
			hikariConfigObj.setMinimumIdle(hikariConfig.getMinimumIdle());
			hikariConfigObj.setConnectionTimeout(hikariConfig.getConnectionTimeout());
			hikariConfigObj.setIdleTimeout(hikariConfig.getIdleTimeout());
			hikariConfigObj.setMaxLifetime(hikariConfig.getMaxLifetime());

			// Disable fail-fast to allow plugin to start even if database is unavailable
			hikariConfigObj.setInitializationFailTimeout(-1);

			return new HikariDataSource(hikariConfigObj);
		} catch (Exception e) {
			handleInitializationError(e, persistence);
			throw new RuntimeException("Failed to initialize HikariCP", e);
		}
	}

	private static void handleInitializationError(Exception e, Persistence persistence) {
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
			Logger.severe("  - Host and port are correct (%s:%d)", persistence.getHost(), persistence.getPort());
			Logger.severe("  - Database server is accepting connections");
			Logger.severe("Database features will be disabled until connection is established.");
			return;
		}

		Logger.severe("Failed to initialize database connection: %s", errorMessage);
		if (cause != null && !errorMessage.equals(cause.getMessage()))
			Logger.severe("Caused by: %s", cause.getMessage());
	}

	private static String maskJdbcUrl(String jdbcUrl) {
		return jdbcUrl.replaceAll("password=[^;&]+", "password=***");
	}

	private static String getDriverClassName(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "org.postgresql.Driver";
			case MARIADB -> "org.mariadb.jdbc.Driver";
		};
	}
}

