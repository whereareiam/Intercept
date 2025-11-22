package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;

import java.util.Locale;

/**
 * Database entity representing a translation for a message entry.
 * Stores locale-specific translations for messages and templates.
 */
@Setter
@Getter
public class MessageTranslationEntity implements SchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Reference to the parent message entry.
	 * Cascade delete handled at entry level.
	 * Unique constraint with locale ensures one translation per locale per entry.
	 */
	private MessageEntryEntity entry;

	/**
	 * Locale for this translation.
	 * Null for single-language entries (empty string in database).
	 */
	private Locale locale;

	/**
	 * Translated text (can be multiline).
	 */
	private String text;

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_translations (
					id %s,
					entry_id BIGINT NOT NULL,
					locale VARCHAR(20) NOT NULL,
					text TEXT NOT NULL,
					CONSTRAINT fk_message_translations_entry
						FOREIGN KEY (entry_id)
						REFERENCES intercept_message_entries (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_message_translations_entry_locale UNIQUE (entry_id, locale)
				)
				""".formatted(idType);
	}

	private String getAutoIncrementPrimaryKey(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}

