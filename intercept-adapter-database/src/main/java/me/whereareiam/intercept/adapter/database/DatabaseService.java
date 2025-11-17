package me.whereareiam.intercept.adapter.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.DependencyResolver;
import me.whereareiam.intercept.adapter.database.initializer.EbeanInitializer;
import me.whereareiam.intercept.adapter.database.initializer.HikariInitializer;
import me.whereareiam.intercept.adapter.database.provider.DatabaseProvider;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.DatabaseConfig;
import me.whereareiam.intercept.type.DatabaseType;

import javax.sql.DataSource;

/**
 * Main orchestrator for database functionality.
 * Handles initialization of dependencies, connection setup, and Ebean configuration.
 */
@Getter
@Singleton
public class DatabaseService implements EventListener {
	private final DatabaseConfig databaseConfig;
	private final Provider<LibraryManager> libraryManagerProvider;
	private final DatabaseProvider databaseProvider;
	private boolean initialized = false;
	private DataSource dataSource;

	@Inject
	public DatabaseService(
			DatabaseConfig databaseConfig,
			Provider<LibraryManager> libraryManagerProvider,
			DatabaseProvider databaseProvider,
			EventManager eventManager
	) {
		this.databaseConfig = databaseConfig;
		this.libraryManagerProvider = libraryManagerProvider;
		this.databaseProvider = databaseProvider;

		eventManager.register(this);
	}

	/**
	 * Initializes the database system.
	 * LibraryManager is obtained from the provider (configured by platform code).
	 */
	@IntercepticEvent(EventOrder.LOWEST)
	public void onReady(InterceptReadyEvent event) {
		if (!databaseConfig.isEnabled()) return;
		if (initialized) throw new IllegalStateException("DatabaseService has already been initialized");

		try {
			DatabaseType type = databaseConfig.getType() != null ? databaseConfig.getType() : DatabaseType.POSTGRES;
			Logger.info("Initializing database connection (%s)...", type);

			// Step 1: Load database dependencies
			initializeDependencies();

			// Step 2: Initialize database connection
			initializeConnection();

			// Step 3: Configure and initialize Ebean
			initializeEbean();

			initialized = true;
			Logger.info("Database connection initialized successfully");
		} catch (Exception ignored) {
		}
	}

	private void initializeDependencies() {
		Logger.info("Loading database dependencies...");
		LibraryManager libraryManager = libraryManagerProvider.get();
		DependencyResolver resolver = new DatabaseDependencyResolver(libraryManager, databaseConfig);
		resolver.loadLibraries();
		resolver.resolveDependencies();

		Logger.info("DatabaseConfig dependencies loaded");
	}

	private void initializeConnection() {
		this.dataSource = HikariInitializer.initialize(databaseConfig);
	}

	private void initializeEbean() {
		Object database = EbeanInitializer.initialize(dataSource, databaseConfig);
		databaseProvider.setDatabase(database);
	}

	/**
	 * Shuts down the database system.
	 * Closes connections and cleans up resources.
	 */
	public void shutdown() {
		if (!initialized || !databaseConfig.isEnabled())
			return;

		try {
			Logger.info("Shutting down database connection...");

			if (databaseProvider.isInitialized())
				EbeanInitializer.shutdown(databaseProvider.getDatabase());

			// Close HikariCP connection pool
			HikariInitializer.shutdown(dataSource);

			initialized = false;
		} catch (Exception e) {
			Logger.warn("Error occurred while shutting down database connection: %s", e.getMessage());
		}
	}
}

