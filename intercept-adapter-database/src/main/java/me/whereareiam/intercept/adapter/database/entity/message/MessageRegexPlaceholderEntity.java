package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;

/**
 * Database entity representing a placeholder mapping for a regex pattern.
 * Stores placeholder names and their corresponding capture group references.
 */
@Getter
@Setter
public class MessageRegexPlaceholderEntity implements SchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Reference to the parent regex pattern.
	 * Cascade delete handled at pattern level.
	 * Unique constraint with placeholderName ensures no duplicate placeholders per pattern.
	 */
	private MessageRegexPatternEntity pattern;

	/**
	 * Placeholder name (e.g., "permission", "player", "name").
	 * Unique constraint with pattern_id ensures no duplicate placeholders per pattern.
	 */
	private String placeholderName;

	/**
	 * Capture group reference (e.g., "$1", "$2", "$3").
	 */
	private String captureGroup;

	@Override
	public String getTableName() {
		return Constants.Database.Tables.MESSAGE_REGEX_PLACEHOLDERS;
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS %s (
					id %s,
					pattern_id BIGINT NOT NULL,
					placeholder_name VARCHAR(100) NOT NULL,
					capture_group VARCHAR(50) NOT NULL,
					CONSTRAINT fk_regex_placeholders_pattern
						FOREIGN KEY (pattern_id)
						REFERENCES %s (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_regex_placeholders UNIQUE (pattern_id, placeholder_name)
				)
				""".formatted(getTableName(), idType, Constants.Database.Tables.MESSAGE_REGEX_PATTERNS);
	}

	private String getAutoIncrementPrimaryKey(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}

