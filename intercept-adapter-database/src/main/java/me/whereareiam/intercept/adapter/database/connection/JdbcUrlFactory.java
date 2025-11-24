package me.whereareiam.intercept.adapter.database.connection;

import me.whereareiam.intercept.model.config.Persistence;

/**
 * Factory for creating JDBC URLs from persistence configuration.
 */
public final class JdbcUrlFactory {
	/**
	 * Creates a JDBC URL from persistence configuration.
	 *
	 * @param persistence the persistence configuration
	 * @return the JDBC URL string
	 */
	public static String create(Persistence persistence) {
		String host = persistence.getHost() + ":" + persistence.getPort();
		String database = persistence.getDatabase();

		return switch (persistence.getType()) {
			case POSTGRES -> String.format("jdbc:postgresql://%s/%s", host, database);
			case MARIADB -> String.format("jdbc:mariadb://%s/%s", host, database);
		};
	}
}

