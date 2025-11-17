package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import me.whereareiam.intercept.adapter.database.provider.DatabaseProvider;

/**
 * Guice configuration module for database adapter.
 * Provides database-related services and bindings.
 */
public class DatabaseConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(DatabaseProvider.class).asEagerSingleton();
		bind(DatabaseService.class).asEagerSingleton();
	}
}