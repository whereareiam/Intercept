package me.whereareiam.intercept.adapter.database.initializer;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import me.whereareiam.intercept.adapter.database.convention.PrefixedUnderscoreNamingConvention;
import me.whereareiam.intercept.logging.Logger;

import javax.sql.DataSource;

/**
 * Initializes Ebean ORM framework.
 * This class is only loaded after database dependencies are available.
 */
public class EbeanInitializer {
	/**
	 * Initializes Ebean with the given DataSource and configuration.
	 *
	 * @param dataSource     the DataSource to use
	 * @param databaseConfig the database configuration
	 * @return the initialized Database instance
	 */
	public static Database initialize(DataSource dataSource, me.whereareiam.intercept.model.config.DatabaseConfig databaseConfig) {
		Logger.info("Initializing Ebean ORM...");

		if (dataSource == null) throw new IllegalStateException("DataSource must be initialized before Ebean");

		try {
			// Create Ebean DatabaseConfig
			DatabaseConfig config = new DatabaseConfig();
			config.setDataSource(dataSource);
			config.setDefaultServer(true);
			config.setName("intercept");

			// Set table prefix if configured
			String tablePrefix = databaseConfig.getTablePrefix();
			if (tablePrefix != null && !tablePrefix.isEmpty()) {
				PrefixedUnderscoreNamingConvention namingConvention = new PrefixedUnderscoreNamingConvention(tablePrefix);
				config.setNamingConvention(namingConvention);
				Logger.info("Table prefix set to: %s", tablePrefix);
			}

			// Create Database instance using DatabaseFactory
			Database database = DatabaseFactory.create(config);

			Logger.info("Ebean ORM initialized successfully");
			return database;
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize Ebean", e);
		}
	}

	/**
	 * Shuts down the Ebean database instance.
	 *
	 * @param database the Database instance to shutdown
	 */
	public static void shutdown(Object database) {
		if (database == null) return;

		try {
			((Database) database).shutdown();
			Logger.info("Ebean database instance shut down");
		} catch (Exception e) {
			Logger.warn("Error shutting down Ebean database: %s", e.getMessage());
		}
	}
}

