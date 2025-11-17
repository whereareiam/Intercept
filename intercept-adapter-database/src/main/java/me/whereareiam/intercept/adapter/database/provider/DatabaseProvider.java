package me.whereareiam.intercept.adapter.database.provider;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;

/**
 * Provider for the Ebean Database instance.
 * The database instance is set by the initializer after dependencies are loaded.
 */
@Getter
@Setter
@Singleton
public class DatabaseProvider {
	private Object database;

	/**
	 * Gets the Database instance.
	 *
	 * @return the Database instance, or null if not initialized
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDatabase() {
		return (T) database;
	}

	/**
	 * Checks if the database is initialized.
	 *
	 * @return true if database is not null
	 */
	public boolean isInitialized() {
		return database != null;
	}
}

