package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.BaseStatementProvider;

import java.util.Arrays;

/**
 * Database-specific SQL adapter for {@link MessageRegexPlaceholderRepository}.
 * Provides SQL statements that are adapted to different database dialects.
 */
public final class MessageRegexPlaceholderAdapter {
	/**
	 * SQL adapter for deleting all regex placeholders.
	 * <p>
	 * PostgreSQL: Uses TRUNCATE with RESTART IDENTITY to reset the sequence.
	 * MariaDB: Uses DELETE followed by ALTER TABLE to reset AUTO_INCREMENT because TRUNCATE
	 * is not allowed on tables with foreign key constraints.
	 */
	public static class TruncateAll extends BaseStatementProvider {
		public TruncateAll() {
			super(
					"TRUNCATE TABLE intercept_message_regex_placeholders RESTART IDENTITY CASCADE",
					Arrays.asList(
							"DELETE FROM intercept_message_regex_placeholders",
							"ALTER TABLE intercept_message_regex_placeholders AUTO_INCREMENT = 1"
					)
			);
		}
	}
}