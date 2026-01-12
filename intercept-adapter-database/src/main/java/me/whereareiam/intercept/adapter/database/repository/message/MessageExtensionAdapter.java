package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.BaseStatementProvider;

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

	/**
	 * SQL adapter for updating extension payloads with JSON casting.
	 */
	public static final class Update extends BaseStatementProvider {
		public Update() {
			super(
					"UPDATE intercept_message_extensions SET payload = CAST(:payload AS JSONB) WHERE id = :id",
					List.of(
							"UPDATE intercept_message_extensions SET payload = :payload WHERE id = :id"
					)
			);
		}
	}
}
