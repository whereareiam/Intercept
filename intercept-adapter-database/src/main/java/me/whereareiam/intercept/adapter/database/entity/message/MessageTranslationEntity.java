package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;

import java.util.Locale;

/**
 * Database entity representing a translation for a message entry.
 * Stores locale-specific translations for messages and templates.
 */
@Getter
@Setter
public class MessageTranslationEntity implements SchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Reference to the parent message entry.
	 * Cascade delete handled at entry level.
	 * Unique constraint with localeString ensures one translation per locale per entry.
	 */
	private MessageEntryEntity entry;

	/**
	 * Locale code (e.g., "en_US", "de_DE", "default").
	 * Stored as string since Locale doesn't have a built-in converter.
	 * Unique constraint with entry_id ensures one translation per locale per entry.
	 */
	private String localeString;

	/**
	 * Translated text (can be multiline).
	 */
	private String text;

	/**
	 * Get the locale object from the locale string.
	 *
	 * @return the Locale object, or null if localeString is null or empty
	 */
	public Locale getLocale() {
		if (localeString == null || localeString.isEmpty())
			return null;

		return Locale.forLanguageTag(localeString.replace('_', '-'));
	}

	/**
	 * Set the locale from a Locale object.
	 *
	 * @param locale the Locale to set
	 */
	public void setLocale(Locale locale) {
		if (locale == null) {
			this.localeString = null;
			return;
		}

		this.localeString = locale.toString();
	}

	@Override
	public String getTableName() {
		return Constants.Database.Tables.MESSAGE_TRANSLATIONS;
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS %s (
					id %s,
					entry_id BIGINT NOT NULL,
					locale_string VARCHAR(20) NOT NULL,
					text TEXT NOT NULL,
					CONSTRAINT fk_message_translations_entry
						FOREIGN KEY (entry_id)
						REFERENCES %s (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_message_translations_entry_locale UNIQUE (entry_id, locale_string)
				)
				""".formatted(getTableName(), idType, Constants.Database.Tables.MESSAGE_ENTRIES);
	}

	private String getAutoIncrementPrimaryKey(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}

