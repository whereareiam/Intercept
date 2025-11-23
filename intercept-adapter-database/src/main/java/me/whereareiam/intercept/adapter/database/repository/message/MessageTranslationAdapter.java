package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.dialect.BaseStatementProvider;

/**
 * Database-specific SQL adapter for {@link MessageTranslationRepository}.
 * Provides SQL statements that are adapted to different database dialects.
 */
public final class MessageTranslationAdapter {
	/**
	 * SQL adapter for deleting all message translations.
	 * <p>
	 * PostgreSQL: Uses TRUNCATE with RESTART IDENTITY to reset the sequence.
	 * MariaDB: Uses TRUNCATE which automatically resets AUTO_INCREMENT.
	 */
	public static class TruncateAll extends BaseStatementProvider {
		public TruncateAll() {
			super(
					"TRUNCATE TABLE intercept_message_translations RESTART IDENTITY CASCADE",
					"TRUNCATE TABLE intercept_message_translations"
			);
		}
	}
}

