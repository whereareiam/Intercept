package me.whereareiam.intercept.adapter.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.adapter.database.provider.DatabaseProvider;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.model.config.Settings;

import javax.sql.DataSource;

/**
 * Default implementation of DatabaseService.
 * Handles database lifecycle and provides access to ConnectionSource.
 */
@Getter
@Singleton
public class DefaultDatabaseService implements DatabaseService, EventListener {
	private final Persistence persistence;
	private final Settings settings;
	private final Provider<LibraryManager> libraryManagerProvider;
	private final DatabaseProvider databaseProvider;
	private boolean initialized = false;
	private ConnectionSource connectionSource;
	private DataSource dataSource;

	@Inject
	public DefaultDatabaseService(
			Persistence persistence,
			Settings settings,
			Provider<LibraryManager> libraryManagerProvider,
			DatabaseProvider databaseProvider,
			EventManager eventManager
	) {
		this.persistence = persistence;
		this.settings = settings;
		this.libraryManagerProvider = libraryManagerProvider;
		this.databaseProvider = databaseProvider;

		eventManager.register(this);
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onReady(InterceptReadyEvent event) {
		if (!persistence.isEnabled()) return;
		if (initialized) throw new IllegalStateException("DatabaseService has already been initialized");

		try {
			DatabaseOrchestrator.InitializationResult result = DatabaseOrchestrator.initialize(persistence, settings, libraryManagerProvider);
			if (result != null) {
				this.dataSource = result.getDataSource();
				this.connectionSource = result.getConnectionSource();
				databaseProvider.setConnectionSource(connectionSource);
				initialized = true;
				Logger.info("Database connection initialized successfully");
			}
		} catch (Exception ignored) {
			// Error already logged by orchestrator
		}
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onShutdown(InterceptShutdownEvent event) {
		if (!initialized || !persistence.isEnabled()) return;

		try {
			Logger.info("Shutting down database connection...");
			DatabaseOrchestrator.shutdown(connectionSource, dataSource);
			initialized = false;
		} catch (Exception e) {
			Logger.warn("Error occurred while shutting down database connection: %s", e.getMessage());
		}
	}

	@Override
	public boolean isInitialized() {
		return initialized && connectionSource != null;
	}
}

