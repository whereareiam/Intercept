package me.whereareiam.intercept.adapter.database.entity.message;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.type.message.MessageType;

/**
 * OrmLite entity representing a message entry (message or template).
 * Stores individual message/template entries within files.
 */
@Getter
@Setter
@DatabaseTable(tableName = "intercept_message_entries")
public class MessageEntryEntity {
	/**
	 * Primary key.
	 */
	@DatabaseField(generatedId = true)
	private Long id;

	/**
	 * Reference to the parent file.
	 * Cascade delete handled at file level.
	 * Unique constraint with entryKey ensures no duplicate entries in same file.
	 */
	@DatabaseField(foreign = true, columnName = "file_id", canBeNull = false, uniqueCombo = true)
	private MessageFileEntity file;

	/**
	 * Entry key within file (e.g., "no-permission", "prefix", "player-name").
	 * Unique constraint with file_id ensures no duplicate entries in same file.
	 */
	@DatabaseField(canBeNull = false, width = 255, uniqueCombo = true)
	private String entryKey;

	/**
	 * Entry-level MessageType: MESSAGE, TEMPLATE, or NULL.
	 * If NULL, inherits from file-level type.
	 */
	@DatabaseField(dataType = DataType.ENUM_STRING, width = 20)
	private MessageType entryType;

	/**
	 * Single-language text (used when no translations exist).
	 * NULL if this entry has translations.
	 */
	@DatabaseField(dataType = DataType.LONG_STRING)
	private String singleText;

	/**
	 * All translations for this entry.
	 * Cascade delete: deleting an entry deletes all its translations.
	 */
	@ForeignCollectionField
	private ForeignCollection<MessageTranslationEntity> translations;

	/**
	 * All regex patterns for this entry.
	 * Cascade delete: deleting an entry deletes all its regex patterns.
	 */
	@ForeignCollectionField(orderColumnName = "sort_order")
	private ForeignCollection<MessageRegexPatternEntity> regexPatterns;

	/**
	 * Get the full key (key prefix + entry key).
	 * Computed from file path and entry key.
	 *
	 * @return full key (e.g., "errors.permissions.no-permission")
	 */
	public String getFullKey() {
		if (file == null || entryKey == null) return entryKey;
		String prefix = file.getKeyPrefix();

		return prefix.isEmpty() ? entryKey : prefix + "." + entryKey;
	}

	/**
	 * Check if this entry has translations.
	 * Determined by checking if translations list is not empty.
	 *
	 * @return true if has translations, false otherwise
	 */
	public boolean hasTranslations() {
		return translations != null && !translations.isEmpty();
	}
}

