package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;
import me.whereareiam.intercept.type.message.MessageType;

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
	 * File-level MessageType: MESSAGE, TEMPLATE, MIXED, or NULL.
	 * If NULL, type is auto-detected from entries.
	 */
	private MessageType fileType;

	/**
	 * All message entries in this file.
	 * Cascade delete: deleting a file deletes all its entries.
	 */
	private List<MessageEntryEntity> entries;

	/**
	 * Get the directory path from file path.
	 * Computed by removing the filename from filePath.
	 *
	 * @return directory path (e.g., "errors" from "errors/permissions.yml")
	 */
	public String getDirectoryPath() {
		if (filePath == null) return "";
		int lastSlash = filePath.lastIndexOf('/');
		return lastSlash > 0 ? filePath.substring(0, lastSlash) : "";
	}

	/**
	 * Get the file name without extension from file path.
	 * Computed by removing directory and extension from filePath.
	 *
	 * @return file name (e.g., "permissions" from "errors/permissions.yml")
	 */
	public String getFileName() {
		if (filePath == null) return "";

		String path = filePath;
		int lastSlash = path.lastIndexOf('/');
		String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
		int lastDot = fileName.lastIndexOf('.');

		return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
	}

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
	public String getTableName() {
		return Constants.Database.Tables.MESSAGE_FILES;
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		String idType = getAutoIncrementPrimaryKey(persistenceType);
		return """
				CREATE TABLE IF NOT EXISTS %s (
					id %s,
					file_path VARCHAR(500) NOT NULL UNIQUE,
					file_type VARCHAR(20)
				)
				""".formatted(getTableName(), idType);
	}

	private String getAutoIncrementPrimaryKey(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> "BIGSERIAL PRIMARY KEY";
			case MARIADB -> "BIGINT AUTO_INCREMENT PRIMARY KEY";
		};
	}
}

