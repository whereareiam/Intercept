package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;

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
	public String getCreateTableStatement(DatabaseType DatabaseType) {
		String idType = getAutoIncrementPrimaryKey(DatabaseType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_regex_placeholders (
					id %s,
					pattern_id BIGINT NOT NULL,
					placeholder_name VARCHAR(100) NOT NULL,
					capture_group VARCHAR(50) NOT NULL,
					CONSTRAINT fk_regex_placeholders_pattern
						FOREIGN KEY (pattern_id)
						REFERENCES intercept_message_regex_patterns (id)
						ON DELETE CASCADE,
					CONSTRAINT uq_regex_placeholders UNIQUE (pattern_id, placeholder_name)
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