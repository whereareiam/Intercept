package me.whereareiam.intercept.adapter.database.entity.message;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

/**
 * OrmLite entity representing a regex pattern for a message entry.
 * Stores regex patterns used for message interception and matching.
 */
@Getter
@Setter
@DatabaseTable(tableName = "intercept_message_regex_patterns")
public class MessageRegexPatternEntity {
	/**
	 * Primary key.
	 */
	@DatabaseField(generatedId = true)
	private Long id;

	/**
	 * Reference to the parent message entry.
	 * Cascade delete handled at entry level.
	 */
	@DatabaseField(foreign = true, columnName = "entry_id", canBeNull = false)
	private MessageEntryEntity entry;

	/**
	 * Regex pattern string to match against.
	 */
	@DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
	private String pattern;

	/**
	 * Pattern priority (higher = checked first).
	 * Default is 0.
	 */
	@DatabaseField(canBeNull = false)
	private int priority = 0;

	/**
	 * Whether to replace only the matched portion of intercepted text.
	 * When false (default), the whole intercepted message is replaced.
	 */
	@DatabaseField(canBeNull = false)
	private boolean replaceMatched = false;

	/**
	 * Order within entry (for preserving pattern order).
	 * Used to maintain the order patterns were defined in the file.
	 */
	@DatabaseField(canBeNull = false, columnName = "sort_order")
	private int sortOrder;

	/**
	 * All placeholders for this regex pattern.
	 * Cascade delete: deleting a pattern deletes all its placeholders.
	 */
	@ForeignCollectionField
	private ForeignCollection<MessageRegexPlaceholderEntity> placeholders;
}

