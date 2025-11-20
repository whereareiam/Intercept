package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import com.j256.ormlite.support.ConnectionSource;
import me.whereareiam.intercept.adapter.database.provider.DatabaseProvider;
import me.whereareiam.intercept.database.DatabaseService;

/**
 * Guice configuration module for database adapter.
 * Provides database-related services and bindings.
 */
public class DatabaseConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(DatabaseProvider.class).asEagerSingleton();
		bind(ConnectionSource.class).toProvider(DatabaseProvider.class);
		bind(DatabaseService.class).to(DefaultDatabaseService.class).asEagerSingleton();
	}
}