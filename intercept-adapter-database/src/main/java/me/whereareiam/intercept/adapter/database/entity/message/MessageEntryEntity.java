package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;
import me.whereareiam.intercept.type.message.MessageType;

import java.util.List;

/**
 * Database entity representing a message entry (message or template).
 * Stores individual message/template entries within files.
 */
@Getter
@Setter
public class MessageEntryEntity implements SchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Reference to the parent file.
	 * Cascade delete handled at file level.
	 * Unique constraint with entryKey ensures no duplicate entries in same file.
	 */
	private MessageFileEntity file;

	/**
	 * Entry key within file (e.g., "no-permission", "prefix", "player-name").
	 * Unique constraint with file_id ensures no duplicate entries in same file.
	 */
	private String entryKey;

	/**
	 * Entry-level MessageType: MESSAGE, TEMPLATE, or NULL.
	 * If NULL, inherits from file-level type.
	 */
	private MessageType entryType;

	/**
	 * Single-language text (used when no translations exist).
	 * NULL if this entry has translations.
	 */
	private String singleText;

	/**
	 * All translations for this entry.
	 * Cascade delete: deleting an entry deletes all its translations.
	 */
	private List<MessageTranslationEntity> translations;

	/**
	 * All regex patterns for this entry.
	 * Cascade delete: deleting an entry deletes all its regex patterns.
	 */
	private List<MessageRegexPatternEntity> regexPatterns;

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

	@Override
	public String getTableName() {
		return Constants.Database.Tables.MESSAGE_ENTRIES;
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS %s (
					id %s,
					file_id BIGINT NOT NULL,
					entry_key VARCHAR(255) NOT NULL,
					entry_type VARCHAR(20),
					single_text TEXT,
					CONSTRAINT fk_message_entries_file
						FOREIGN KEY (file_id)
						REFERENCES %s (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_message_entries_file_key UNIQUE (file_id, entry_key)
				)
				""".formatted(getTableName(), idType, Constants.Database.Tables.MESSAGE_FILES);
	}

	private String getAutoIncrementPrimaryKey(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}

