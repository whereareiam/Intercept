package me.whereareiam.intercept.adapter.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.adapter.persistence.store.DatabaseLanguageStore;
import me.whereareiam.intercept.adapter.persistence.store.DatabaseSharedStore;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.persistence.LanguageStore;
import me.whereareiam.intercept.persistence.SharedStore;
import me.whereareiam.intercept.persistence.database.DatabaseService;
import me.whereareiam.intercept.persistence.database.Persistence;

/**
 * Binds the database and the stores on it.
 */
public final class PersistenceConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Persistence.class).toProvider(PersistenceProvider.class);
		bind(DatabaseService.class).to(DefaultDatabaseService.class);
		bind(SharedStore.class).to(DatabaseSharedStore.class);
		bind(LanguageStore.class).to(DatabaseLanguageStore.class);

		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(DefaultDatabaseService.class);
	}
}
