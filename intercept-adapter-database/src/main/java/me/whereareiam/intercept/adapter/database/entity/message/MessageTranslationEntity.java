package me.whereareiam.intercept.adapter.database.entity.message;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * OrmLite entity representing a translation for a message entry.
 * Stores locale-specific translations for messages and templates.
 */
@Getter
@Setter
@DatabaseTable(tableName = "intercept_message_translations")
public class MessageTranslationEntity {
	/**
	 * Primary key.
	 */
	@DatabaseField(generatedId = true)
	private Long id;

	/**
	 * Reference to the parent message entry.
	 * Cascade delete handled at entry level.
	 * Unique constraint with localeString ensures one translation per locale per entry.
	 */
	@DatabaseField(foreign = true, columnName = "entry_id", canBeNull = false, uniqueCombo = true)
	private MessageEntryEntity entry;

	/**
	 * Locale code (e.g., "en_US", "de_DE", "default").
	 * Stored as string since Locale doesn't have a built-in converter.
	 * Unique constraint with entry_id ensures one translation per locale per entry.
	 */
	@DatabaseField(canBeNull = false, width = 20, uniqueCombo = true)
	private String localeString;

	/**
	 * Translated text (can be multiline).
	 */
	@DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
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
}

