package me.whereareiam.intercept.database;

/**
 * Service interface for database operations.
 * Provides access to database functionality and connection management.
 * <p>
 * For database access, use {@code JdbiSingleton.get()} to obtain the Jdbi instance.
 * The singleton is set after dependencies are loaded during initialization.
 */
public interface DatabaseService {
	/**
	 * Checks if the database is initialized and ready to use.
	 *
	 * @return true if the database is initialized, false otherwise
	 */
	boolean isInitialized();
}

