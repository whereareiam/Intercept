package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;

import java.util.List;

/**
 * Database entity representing a regex pattern for a message entry.
 * Stores regex patterns used for message interception and matching.
 */
@Getter
@Setter
public class MessageRegexPatternEntity implements SchemaProvider {
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
	 * Regex pattern string to match against.
	 */
	private String pattern;

	/**
	 * Pattern priority (higher = checked first).
	 * Default is 0.
	 */
	private int priority = 0;

	/**
	 * Whether to replace only the matched portion of intercepted text.
	 * When false (default), the whole intercepted message is replaced.
	 */
	private boolean replaceMatched = false;

	/**
	 * Order within entry (for preserving pattern order).
	 * Used to maintain the order patterns were defined in the file.
	 */
	private int sortOrder;

	/**
	 * All placeholders for this regex pattern.
	 * Cascade delete: deleting a pattern deletes all its placeholders.
	 */
	private List<MessageRegexPlaceholderEntity> placeholders;

	@Override
	public String getCreateTableStatement(DatabaseType DatabaseType) {
		String idType = getAutoIncrementPrimaryKey(DatabaseType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_regex_patterns (
					id %s,
					entry_id BIGINT NOT NULL,
					pattern TEXT NOT NULL,
					priority INT NOT NULL DEFAULT 0,
					replace_matched BOOLEAN NOT NULL DEFAULT FALSE,
					sort_order INT NOT NULL,
					CONSTRAINT fk_regex_patterns_entry
						FOREIGN KEY (entry_id)
						REFERENCES intercept_message_entries (id)
						ON DELETE CASCADE
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