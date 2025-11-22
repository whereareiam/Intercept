package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;

import java.util.List;

/**
 * Database entity representing a message file.
 * Stores file-level metadata for message/template files.
 */
@Getter
@Setter
public class MessageFileEntity implements SchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Relative path from messages root (e.g., "errors/permissions.yml", "common/styles.yml").
	 * Unique constraint ensures no duplicate files.
	 */
	private String filePath;

	/**
	 * All message entries in this file.
	 * Cascade delete: deleting a file deletes all its entries.
	 */
	private List<MessageEntryEntity> entries;

	/**
	 * Get the key prefix from file path.
	 * Computed by removing extension and replacing path separators with dots.
	 *
	 * @return key prefix (e.g., "errors.permissions" from "errors/permissions.yml")
	 */
	public String getKeyPrefix() {
		if (filePath == null) return "";

		// Remove extension
		String path = filePath;
		int lastDot = path.lastIndexOf('.');

		if (lastDot > 0) path = path.substring(0, lastDot);

		// Replace path separators with dots
		return path.replace('\\', '.').replace('/', '.');
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_files (
					id %s,
					file_path VARCHAR(500) NOT NULL UNIQUE
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

