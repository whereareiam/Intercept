package me.whereareiam.intercept.adapter.database.connection;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import me.whereareiam.intercept.adapter.database.connection.type.SafePostgresDatabaseType;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.type.PersistenceType;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Factory for creating ConnectionSource instances.
 */
public final class ConnectionSourceFactory {
	/**
	 * Creates a ConnectionSource from a DataSource and persistence configuration.
	 *
	 * @param dataSource  the DataSource to use
	 * @param persistence the persistence configuration
	 * @return the created ConnectionSource
	 * @throws SQLException if the connection source cannot be created
	 */
	public static ConnectionSource create(DataSource dataSource, Persistence persistence) throws SQLException {
		if (dataSource == null) throw new IllegalStateException("DataSource must not be null");
		if (persistence == null) throw new IllegalStateException("Persistence configuration must not be null");

		String jdbcUrl = JdbcUrlFactory.create(persistence);
		PersistenceType type = persistence.getType() != null ? persistence.getType() : PersistenceType.POSTGRES;

		if (type == PersistenceType.POSTGRES) {
			DataSourceConnectionSource connectionSource = new DataSourceConnectionSource();
			connectionSource.setDataSource(dataSource);
			connectionSource.setDatabaseUrl(jdbcUrl);
			connectionSource.setDatabaseType(new SafePostgresDatabaseType());
			connectionSource.initialize();
			return connectionSource;
		}

		return new DataSourceConnectionSource(dataSource, jdbcUrl);
	}
}

