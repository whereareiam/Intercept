package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.EntitySchemaProvider;
import me.whereareiam.dialectica.annotation.Entity;
import me.whereareiam.dialectica.type.DatabaseType;

/**
 * Database entity representing extension payloads for a message entry.
 */
@Getter
@Setter
@Entity(tableName = "intercept_message_extensions", dependsOn = {
		MessageEntryEntity.class
})
public class MessageExtensionEntity implements EntitySchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Reference to the parent message entry.
	 * Cascade delete handled at entry level.
	 */
	private MessageEntryEntity entry;

	/**
	 * Extension id (e.g. "interception").
	 */
	private String extensionId;

	/**
	 * Serialized payload for the extension.
	 */
	private String payload;

	@Override
	public String statement(DatabaseType databaseType) {
		String idType = getAutoIncrementPrimaryKey(databaseType);
		String payloadType = getPayloadType(databaseType);

		return """
				CREATE TABLE IF NOT EXISTS intercept_message_extensions (
					id %s,
					entry_id BIGINT NOT NULL,
					extension_id VARCHAR(120) NOT NULL,
					payload %s NOT NULL,
					CONSTRAINT fk_message_extensions_entry
						FOREIGN KEY (entry_id)
						REFERENCES intercept_message_entries (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_message_extensions_entry UNIQUE (entry_id, extension_id)
				)
				""".formatted(idType, payloadType);
	}

	private String getAutoIncrementPrimaryKey(DatabaseType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}

	private String getPayloadType(DatabaseType type) {
		return switch (type) {
			case POSTGRES -> "JSONB";
			case MARIADB -> "JSON";
		};
	}
}
