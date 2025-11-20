package me.whereareiam.intercept.adapter.database;

import com.google.inject.Provider;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.DependencyResolver;
import me.whereareiam.intercept.adapter.database.config.LoggerConfig;
import me.whereareiam.intercept.adapter.database.connection.ConnectionSourceFactory;
import me.whereareiam.intercept.adapter.database.connection.DataSourceFactory;
import me.whereareiam.intercept.adapter.database.schema.SchemaManager;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.model.config.Settings;

import javax.sql.DataSource;

/**
 * Orchestrates database initialization and shutdown lifecycle.
 * Coordinates dependency loading, connection setup, and schema creation.
 */
public final class DatabaseOrchestrator {
	/**
	 * Result of database initialization containing both DataSource and ConnectionSource.
	 */
	@Getter
	@RequiredArgsConstructor
	public static class InitializationResult {
		private final DataSource dataSource;
		private final ConnectionSource connectionSource;
	}

	/**
	 * Initializes the database system.
	 *
	 * @param persistence            the persistence configuration
	 * @param settings               the application settings
	 * @param libraryManagerProvider provider for LibraryManager
	 * @return the initialization result containing DataSource and ConnectionSource
	 */
	public static InitializationResult initialize(
			Persistence persistence,
			Settings settings,
			com.google.inject.Provider<LibraryManager> libraryManagerProvider
	) {
		if (!persistence.isEnabled()) {
			return null;
		}

		try {
			// Step 1: Load database dependencies
			loadDependencies(persistence, libraryManagerProvider);

			// Step 2: Create DataSource
			DataSource dataSource = DataSourceFactory.create(persistence);

			// Step 3: Configure logger
			LoggerConfig.configure(settings);

			// Step 4: Create ConnectionSource
			ConnectionSource connectionSource = ConnectionSourceFactory.create(dataSource, persistence);

			// Step 5: Create database tables
			SchemaManager.createTables(connectionSource);

			Logger.info("Database initialized successfully");
			return new InitializationResult(dataSource, connectionSource);
		} catch (Exception e) {
			Logger.severe("Failed to initialize database: %s", e.getMessage());
			throw new RuntimeException("Failed to initialize database", e);
		}
	}

	/**
	 * Shuts down the database system.
	 *
	 * @param connectionSource the ConnectionSource to close
	 * @param dataSource       the DataSource to close
	 */
	public static void shutdown(ConnectionSource connectionSource, DataSource dataSource) {
		if (connectionSource == null && dataSource == null)
			return;

		if (connectionSource != null) {
			try {
				connectionSource.close();
				Logger.info("Connection source closed");
			} catch (Exception e) {
				Logger.warn("Error shutting down connection source: %s", e.getMessage());
			}
		}

		if (dataSource instanceof HikariDataSource hikariDataSource) {
			try {
				hikariDataSource.close();
				Logger.info("Database connection pool closed");
			} catch (Exception e) {
				Logger.warn("Error closing connection pool: %s", e.getMessage());
			}
		}
	}

	/**
	 * Loads database dependencies using LibraryManager.
	 */
	private static void loadDependencies(
			Persistence persistence,
			Provider<LibraryManager> libraryManagerProvider
	) {
		Logger.info("Loading database dependencies...");
		LibraryManager libraryManager = libraryManagerProvider.get();
		DependencyResolver resolver = new DatabaseDependencyResolver(libraryManager, persistence);
		resolver.loadLibraries();
		resolver.resolveDependencies();
	}
}

