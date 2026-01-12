package me.whereareiam.intercept.adapter.database.entity.message;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.EntitySchemaProvider;
import me.whereareiam.dialectica.annotation.Entity;
import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.util.NamespaceUtil;

import java.util.List;

/**
 * Database entity representing a message file.
 * Stores file-level metadata for message/template files.
 */
@Getter
@Setter
@Entity(tableName = "intercept_message_files", version = 2)
public class MessageFileEntity implements EntitySchemaProvider {
	/**
	 * Primary key.
	 */
	private Long id;

	/**
	 * Namespace for this file (e.g., "intercept").
	 */
	private String namespace;

	/**
	 * Relative path from messages root without file extension (e.g., "errors/permissions", "common/styles").
	 * File extension is determined dynamically per server through ConfiguraBootstrap and is not stored.
	 * Unique constraint ensures no duplicate files.
	 */
	private String filePath;

	/**
	 * Format id for this file (e.g., "MULTI_LOCALE").
	 */
	private String fileType;

	/**
	 * All message entries in this file.
	 * Cascade delete: deleting a file deletes all its entries.
	 */
	private List<MessageEntryEntity> entries;

	/**
	 * Get the key prefix from file path.
	 * Computed by replacing path separators with dots.
	 * Note: File path does not include extension (it's determined dynamically per server).
	 *
	 * @return key prefix (e.g., "errors.permissions" from "errors/permissions")
	 */
	public String getKeyPrefix() {
		if (filePath == null) return "";

		// Replace path separators with dots
		String prefix = filePath.replace('\\', '.').replace('/', '.');
		return NamespaceUtil.qualify(namespace, prefix);
	}

	@Override
	public String statement(DatabaseType databaseType) {
		String idType = getAutoIncrementPrimaryKey(databaseType);
		return """
				CREATE TABLE IF NOT EXISTS intercept_message_files (
					id %s,
					namespace VARCHAR(100) NOT NULL,
					file_path VARCHAR(500) NOT NULL,
					file_type VARCHAR(50),
					CONSTRAINT uq_message_files_namespace_path UNIQUE (namespace, file_path)
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
