package me.whereareiam.intercept.adapter.database.connection;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import me.whereareiam.intercept.model.config.Persistence;

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

		String jdbcUrl = JdbcUrlFactory.create(persistence);
		return new DataSourceConnectionSource(dataSource, jdbcUrl);
	}
}

