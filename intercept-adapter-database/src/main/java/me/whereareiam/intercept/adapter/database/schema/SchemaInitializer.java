package me.whereareiam.intercept.adapter.database.schema;

import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.type.PersistenceType;
import org.jdbi.v3.core.Jdbi;

/**
 * Initializes database schema by creating all tables.
 * Entities are self-aware and provide their own DDL statements.
 */
public final class SchemaInitializer {
	private static final SchemaProvider[] ENTITIES = {
			new PlayerEntity(),
			new MessageFileEntity(),
			new MessageEntryEntity(),
			new MessageTranslationEntity(),
			new MessageRegexPatternEntity(),
			new MessageRegexPlaceholderEntity()
	};

	/**
	 * Creates all database tables in dependency order.
	 * Entities are self-aware and provide their own DDL statements.
	 *
	 * @param jdbi shared Jdbi instance
	 * @param type the persistence type (POSTGRES or MARIADB)
	 */
	public static void createTables(Jdbi jdbi, PersistenceType type) {
		if (jdbi == null) throw new IllegalStateException("Jdbi instance must not be null");
		if (type == null) throw new IllegalStateException("PersistenceType must not be null");

		jdbi.useHandle(handle -> handle.useTransaction(transactionHandle -> {
			for (SchemaProvider entity : ENTITIES) {
				String ddl = entity.getCreateTableStatement(type);
				transactionHandle.execute(ddl);
			}
		}));
	}
}