package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MessageFileEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 * Note: Table names use default values. If table names are customized, they must match the default.
 */
@RegisterBeanMapper(MessageFileEntity.class)
public interface MessageFileRepository {
	@SqlQuery("SELECT * FROM intercept_message_files WHERE id = :id")
	Optional<MessageFileEntity> findById(@Bind("id") long id);

	@SqlQuery("SELECT * FROM intercept_message_files WHERE namespace = :namespace AND file_path = :filePath")
	Optional<MessageFileEntity> findByFilePathAndNamespace(
			@Bind("namespace") String namespace,
			@Bind("filePath") String filePath
	);

	@SqlQuery("SELECT * FROM intercept_message_files")
	List<MessageFileEntity> findAll();

	@SqlQuery("SELECT * FROM intercept_message_files WHERE namespace = :namespace")
	List<MessageFileEntity> findAllByNamespace(@Bind("namespace") String namespace);

	@GetGeneratedKeys("id")
	@SqlUpdate("INSERT INTO intercept_message_files (namespace, file_path, file_type) VALUES (:namespace, :filePath, :fileType)")
	long insert(
			@Bind("namespace") String namespace,
			@Bind("filePath") String filePath,
			@Bind("fileType") String fileType
	);

	@SqlUpdate("UPDATE intercept_message_files SET namespace = :namespace, file_path = :filePath, file_type = :fileType WHERE id = :id")
	void update(
			@Bind("id") long id,
			@Bind("namespace") String namespace,
			@Bind("filePath") String filePath,
			@Bind("fileType") String fileType
	);

	@SqlUpdate("DELETE FROM intercept_message_files WHERE id = :id")
	void deleteById(@Bind("id") long id);

	@SqlQuery("SELECT COUNT(*) > 0 FROM intercept_message_files WHERE id = :id")
	boolean existsById(@Bind("id") long id);

	@SqlQuery("SELECT COUNT(*) > 0 FROM intercept_message_files WHERE namespace = :namespace AND file_path = :filePath")
	boolean existsByFilePathAndNamespace(
			@Bind("namespace") String namespace,
			@Bind("filePath") String filePath
	);

	@SqlQuery("SELECT COUNT(*) FROM intercept_message_files")
	long count();

	/**
	 * Convenience method to save (insert or update) a file entity.
	 */
	default MessageFileEntity save(MessageFileEntity entity) {
		if (entity.getId() == null) {
			long id = insert(entity.getNamespace(), entity.getFilePath(), entity.getFileType());
			entity.setId(id);

			return entity;
		}

		update(entity.getId(), entity.getNamespace(), entity.getFilePath(), entity.getFileType());

		return entity;
	}
}
