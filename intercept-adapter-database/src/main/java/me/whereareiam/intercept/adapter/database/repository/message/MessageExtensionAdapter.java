package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.BaseStatementProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Database-specific SQL adapter for {@link MessageExtensionRepository}.
 */
public final class MessageExtensionAdapter {
	/**
	 * SQL adapter for inserting extension payloads with JSON casting.
	 */
	public static final class Insert extends BaseStatementProvider {
		public Insert() {
			super(
					"INSERT INTO intercept_message_extensions (entry_id, extension_id, payload) " +
							"VALUES (:entryId, :extensionId, CAST(:payload AS JSONB))",
					List.of(
							"INSERT INTO intercept_message_extensions (entry_id, extension_id, payload) " +
									"VALUES (:entryId, :extensionId, :payload)"
					)
			);
		}
	}
}
