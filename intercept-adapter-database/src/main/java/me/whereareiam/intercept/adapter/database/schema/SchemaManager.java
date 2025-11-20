package me.whereareiam.intercept.adapter.database.schema;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Manages database schema creation.
 */
public final class SchemaManager {
	/**
	 * Creates all database tables from the entity registry.
	 * Tables are created in dependency order to ensure parent tables exist before children.
	 *
	 * @param connectionSource the connection source
	 * @throws SQLException if table creation fails
	 */
	public static void createTables(ConnectionSource connectionSource) throws SQLException {
		for (Class<?> entityClass : EntityRegistry.getEntities()) {
			TableUtils.createTableIfNotExists(connectionSource, entityClass);
		}
	}
}

