package me.whereareiam.intercept.adapter.database.dialect;

import me.whereareiam.intercept.type.PersistenceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Base class for {@link StatementProvider} implementations that eliminates
 * the need for repetitive switch statements.
 * <p>
 * Subclasses can provide SQL strings for each database type, or use the same
 * SQL for all databases by providing a single value.
 * <p>
 * Example usage:
 * <pre>{@code
 * public static class TruncateAll extends BaseStatementProvider {
 *     public TruncateAll() {
 *         super("TRUNCATE TABLE intercept_message_files RESTART IDENTITY CASCADE",
 *               "TRUNCATE TABLE intercept_message_files");
 *     }
 * }
 * }</pre>
 * <p>
 * Or for identical SQL across databases:
 * <pre>{@code
 * public static class FindById extends BaseStatementProvider {
 *     public FindById() {
 *         super("SELECT * FROM intercept_message_files WHERE id = :id");
 *     }
 * }
 * }</pre>
 */
public abstract class BaseStatementProvider implements StatementProvider {
	private final Map<PersistenceType, String> sqlMap;

	/**
	 * Creates a provider with the same SQL for all database types.
	 *
	 * @param sql the SQL statement to use for all database types
	 */
	protected BaseStatementProvider(String sql) {
		this.sqlMap = new EnumMap<>(PersistenceType.class);
		for (PersistenceType type : PersistenceType.values()) {
			this.sqlMap.put(type, sql);
		}
	}

	/**
	 * Creates a provider with database-specific SQL.
	 * The order of arguments is: POSTGRES, MARIADB
	 *
	 * @param postgresSql the SQL statement for PostgreSQL
	 * @param mariaDbSql  the SQL statement for MariaDB
	 */
	protected BaseStatementProvider(String postgresSql, String mariaDbSql) {
		this.sqlMap = new EnumMap<>(PersistenceType.class);
		this.sqlMap.put(PersistenceType.POSTGRES, postgresSql);
		this.sqlMap.put(PersistenceType.MARIADB, mariaDbSql);
	}

	@Override
	public final String getStatemenet(PersistenceType persistenceType) {
		String sql = sqlMap.get(persistenceType);
		if (sql == null) throw new IllegalStateException("No SQL defined for PersistenceType: " + persistenceType);

		return sql;
	}
}

