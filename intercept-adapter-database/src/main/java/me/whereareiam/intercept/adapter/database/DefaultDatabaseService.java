package me.whereareiam.intercept.adapter.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.adapter.database.config.LoggerConfig;
import me.whereareiam.intercept.adapter.database.connection.DataSourceFactory;
import me.whereareiam.intercept.adapter.database.schema.SchemaInitializer;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Persistence;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;

/**
 * Default implementation of DatabaseService.
 * Handles database lifecycle and provides access to Jdbi.
 */
@Getter
@Singleton
public class DefaultDatabaseService implements DatabaseService, EventListener {
	private final Persistence persistence;
	private final Provider<LibraryManager> libraryManagerProvider;
	private final Boolean databaseReady;
	private boolean initialized = false;
	private DataSource dataSource;
	private Jdbi jdbi;

	@Inject
	public DefaultDatabaseService(
			Persistence persistence,
			Provider<LibraryManager> libraryManagerProvider,
			@Named("databaseReady") Boolean databaseReady,
			EventManager eventManager
	) {
		this.persistence = persistence;
		this.libraryManagerProvider = libraryManagerProvider;
		this.databaseReady = databaseReady;

		eventManager.register(this);
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onReady(InterceptReadyEvent event) {
		if (!databaseReady) return;
		if (initialized) throw new IllegalStateException("DatabaseService has already been initialized");

		try {
			// Step 1: Create DataSource
			this.dataSource = DataSourceFactory.create(persistence);

			// Step 2: Initialize Jdbi (only loaded after dependencies are available)
			this.jdbi = Jdbi.create(dataSource);
			jdbi.installPlugin(new SqlObjectPlugin());

			// Step 3: Configure logger
			LoggerConfig.configure(jdbi);

			// Step 4: Create database tables
			SchemaInitializer.createTables(jdbi);
			initialized = true;
		} catch (Exception e) {
			Logger.severe("Failed to initialize database: %s", e.getMessage());
			throw new RuntimeException("Failed to initialize database", e);
		}
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onShutdown(InterceptShutdownEvent event) {
		if (!initialized || !databaseReady) return;

		try {
			Logger.info("Shutting down database connection...");

			if (dataSource instanceof HikariDataSource hikariDataSource) {
				hikariDataSource.close();
				Logger.info("Database connection pool closed");
			}

			dataSource = null;
			jdbi = null;
			initialized = false;
		} catch (Exception e) {
			Logger.warn("Error occurred while shutting down database connection: %s", e.getMessage());
		}
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
}

