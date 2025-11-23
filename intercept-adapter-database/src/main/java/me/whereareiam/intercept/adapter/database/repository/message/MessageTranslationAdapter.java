package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.BaseStatementProvider;

import java.util.Arrays;

/**
 * Database-specific SQL adapter for {@link MessageTranslationRepository}.
 * Provides SQL statements that are adapted to different database dialects.
 */
public final class MessageTranslationAdapter {
	/**
	 * SQL adapter for deleting all message translations.
	 * <p>
	 * PostgreSQL: Uses TRUNCATE with RESTART IDENTITY to reset the sequence.
	 * MariaDB: Uses DELETE followed by ALTER TABLE to reset AUTO_INCREMENT because TRUNCATE
	 * is not allowed on tables referenced by foreign key constraints.
	 */
	public static class TruncateAll extends BaseStatementProvider {
		public TruncateAll() {
			super(
					"TRUNCATE TABLE intercept_message_translations RESTART IDENTITY CASCADE",
					Arrays.asList(
							"DELETE FROM intercept_message_translations",
							"ALTER TABLE intercept_message_translations AUTO_INCREMENT = 1"
					)
			);
		}
	}
}