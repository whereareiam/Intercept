package me.whereareiam.intercept.database;

/**
 * Service interface for database operations.
 * Provides access to database functionality and connection management.
 * <p>
 * For accessing ConnectionSource, inject {@code Provider<ConnectionSource>}
 * or {@code ConnectionSource} directly via Guice, using DatabaseProvider.
 */
public interface DatabaseService {
	/**
	 * Checks if the database is initialized and ready to use.
	 *
	 * @return true if the database is initialized, false otherwise
	 */
	boolean isInitialized();
}

