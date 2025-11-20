package me.whereareiam.intercept.adapter.database.provider;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import lombok.Setter;

/**
 * Provider for the OrmLite ConnectionSource instance.
 * Implements Guice's Provider interface for lazy initialization.
 * The connection source is set by the initializer after dependencies are loaded.
 */
@Getter
@Setter
@Singleton
public class DatabaseProvider implements Provider<ConnectionSource> {
	private ConnectionSource connectionSource;

	/**
	 * Gets the ConnectionSource instance.
	 * This method is called by Guice when ConnectionSource is injected.
	 *
	 * @return the ConnectionSource instance, or null if not initialized
	 * @throws IllegalStateException if ConnectionSource is not yet initialized
	 */
	@Override
	public ConnectionSource get() {
		if (connectionSource == null)
			throw new IllegalStateException("ConnectionSource has not been initialized yet. " +
					"Ensure database initialization is complete before accessing the ConnectionSource.");

		return connectionSource;
	}
}

