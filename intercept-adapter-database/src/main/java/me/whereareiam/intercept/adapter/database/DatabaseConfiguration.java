package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.adapter.database.provider.JdbiProvider;
import me.whereareiam.intercept.database.DatabaseService;
import org.jdbi.v3.core.Jdbi;

/**
 * Guice configuration module for database adapter.
 * Provides database-related services and bindings.
 */
public class DatabaseConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		requestInjection(this);

		bind(DatabaseService.class).to(DefaultDatabaseService.class).asEagerSingleton();
		bind(Jdbi.class).toProvider(JdbiProvider.class).in(Singleton.class);
	}

	/**
	 * Loads database dependencies and provides a boolean indicating if they were loaded.
	 * This method is called by Guice during injection (as eager singleton),
	 * ensuring Jdbi classes are available before any Jdbi-related bindings are processed.
	 * <p>
	 * Other classes can inject this boolean with @Named("databaseReady")
	 * to check if dependencies are available.
	 *
	 * @param libraryManager the LibraryManager instance
	 * @return true if dependencies were loaded, false otherwise
	 */
	@Provides
	@Singleton
	@Named("databaseReady")
	private Boolean loadDatabaseDependencies(LibraryManager libraryManager) {
		try {
			DatabaseDependencyResolver resolver = new DatabaseDependencyResolver(libraryManager);
			resolver.loadLibraries();
			resolver.resolveDependencies();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}