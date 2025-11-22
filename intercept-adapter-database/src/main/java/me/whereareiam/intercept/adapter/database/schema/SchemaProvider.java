package me.whereareiam.intercept.adapter.database.schema;

import me.whereareiam.intercept.type.PersistenceType;

/**
 * Interface for entities that can provide their own database schema DDL statements.
 * This allows entities to be self-aware and manage their own table creation.
 */
public interface SchemaProvider {
	/**
	 * Gets the CREATE TABLE DDL statement for this entity.
	 * The statement should use "CREATE TABLE IF NOT EXISTS" to be idempotent.
	 *
	 * @param persistenceType the database type (affects syntax like auto-increment)
	 * @return the DDL statement for creating the table
	 */
	String getCreateTableStatement(PersistenceType persistenceType);
}

