package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.EntitySchemaProvider;
import me.whereareiam.dialectica.annotation.Entity;
import me.whereareiam.dialectica.type.DatabaseType;

/**
 * Database entity representing a template text for a message entry.
 */
@Getter
@Setter
@Entity(tableName = "intercept_message_templates", dependsOn = {
		MessageEntryEntity.class
})
public class MessageTemplateEntity implements EntitySchemaProvider {
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
	 * Template text (can be multiline).
	 */
	private String text;

	@Override
	public String statement(DatabaseType databaseType) {
		String idType = getAutoIncrementPrimaryKey(databaseType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_templates (
					id %s,
					entry_id BIGINT NOT NULL,
					text TEXT NOT NULL,
					CONSTRAINT fk_message_templates_entry
						FOREIGN KEY (entry_id)
						REFERENCES intercept_message_entries (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_message_templates_entry UNIQUE (entry_id)
				)
				""".formatted(idType);
	}

	private String getAutoIncrementPrimaryKey(DatabaseType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}
