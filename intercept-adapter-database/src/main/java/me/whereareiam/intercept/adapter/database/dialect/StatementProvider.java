package me.whereareiam.intercept.adapter.database.dialect;

import me.whereareiam.intercept.type.PersistenceType;

/**
 * Interface for providing database-specific SQL statements.
 * Similar to {@link me.whereareiam.intercept.adapter.database.schema.SchemaProvider},
 * but for DML statements (SELECT, INSERT, UPDATE, DELETE) rather than DDL.
 * <p>
 * Implementations should provide SQL strings that are specific to each database type.
 * For statements that are identical across databases, simply return the same SQL for all types.
 */
public interface StatementProvider {
	/**
	 * Gets the SQL statement string for a specific database type.
	 *
	 * @param persistenceType the database type (POSTGRES or MARIADB)
	 * @return the SQL statement string appropriate for the given database type
	 */
	String getStatemenet(PersistenceType persistenceType);
}

