package me.whereareiam.intercept.adapter.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.whereareiam.dialectica.DialectPlugin;
import me.whereareiam.dialectica.Dialectica;
import me.whereareiam.dialectica.SchemaManager;
import me.whereareiam.intercept.adapter.database.config.LoggerConfig;
import me.whereareiam.intercept.adapter.database.connection.DataSourceFactory;
import me.whereareiam.intercept.adapter.database.converter.LocaleArgumentFactory;
import me.whereareiam.intercept.adapter.database.converter.LocaleColumnMapper;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Persistence;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
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
	private final MessagePersistenceService messagePersistenceService;
	private boolean initialized = false;
	private DataSource dataSource;
	private Jdbi jdbi;

	@Inject
	public DefaultDatabaseService(
			Persistence persistence,
			EventManager eventManager,
			MessagePersistenceService messagePersistenceService
	) {
		this.persistence = persistence;
		this.messagePersistenceService = messagePersistenceService;
		eventManager.register(this);
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onReady(InterceptReadyEvent event) {
		if (!persistence.isEnabled()) return;
		if (initialized) throw new IllegalStateException("DatabaseService has already been initialized");

		try {
			this.dataSource = DataSourceFactory.create(persistence);

			this.jdbi = Jdbi.create(dataSource);
			jdbi.installPlugin(new SqlObjectPlugin());
			jdbi.installPlugin(new DialectPlugin(persistence.getType()));
			jdbi.registerColumnMapper(new LocaleColumnMapper());
			jdbi.getConfig().get(Arguments.class).register(new LocaleArgumentFactory());

			LoggerConfig.configure(jdbi);

			// Initialize schema using Dialectica with automatic package scanning
			SchemaManager schemaManager = Dialectica.schema(jdbi)
					.scanPackages("me.whereareiam.intercept.adapter.database.entity")
					.setFailOnError(false);
			schemaManager.initialize();

			initialized = true;
		} catch (Exception e) {
			Logger.severe("Failed to initialize database: %s", e.getMessage());
			throw new RuntimeException("Failed to initialize database", e);
		}
	}

	@IntercepticEvent(EventOrder.LOW)
	public void onReadyAfter(InterceptReadyEvent event) {
		if (!persistence.isEnabled() || !persistence.isAutoDownloadOnStartup()) return;
		if (!initialized) {
			Logger.warn("Auto-download requested but database is not initialized yet");
			return;
		}

		try {
			Logger.info("Auto-downloading messages from database...");
			messagePersistenceService.downloadMessages();
			Logger.info("Messages downloaded from database into storage");
		} catch (Exception e) {
			Logger.warn("Failed to auto-download messages: %s", e.getMessage());
		}
	}

	@IntercepticEvent(EventOrder.LOWEST)
	public void onShutdown(InterceptShutdownEvent event) {
		if (!initialized || !persistence.isEnabled()) return;

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

