package me.whereareiam.intercept.adapter.database.entity.message;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

/**
 * OrmLite entity representing a placeholder mapping for a regex pattern.
 * Stores placeholder names and their corresponding capture group references.
 */
@Getter
@Setter
@DatabaseTable(tableName = "intercept_message_regex_placeholders")
public class MessageRegexPlaceholderEntity {
	/**
	 * Primary key.
	 */
	@DatabaseField(generatedId = true)
	private Long id;

	/**
	 * Reference to the parent regex pattern.
	 * Cascade delete handled at pattern level.
	 * Unique constraint with placeholderName ensures no duplicate placeholders per pattern.
	 */
	@DatabaseField(foreign = true, columnName = "pattern_id", canBeNull = false, uniqueCombo = true)
	private MessageRegexPatternEntity pattern;

	/**
	 * Placeholder name (e.g., "permission", "player", "name").
	 * Unique constraint with pattern_id ensures no duplicate placeholders per pattern.
	 */
	@DatabaseField(canBeNull = false, width = 100, uniqueCombo = true)
	private String placeholderName;

	/**
	 * Capture group reference (e.g., "$1", "$2", "$3").
	 */
	@DatabaseField(canBeNull = false, width = 50)
	private String captureGroup;
}

