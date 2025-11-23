package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.statement.DialectUpdate;
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

	@SqlQuery("SELECT * FROM intercept_message_files WHERE file_path = :filePath")
	Optional<MessageFileEntity> findByFilePath(@Bind("filePath") String filePath);

	@SqlQuery("SELECT * FROM intercept_message_files")
	List<MessageFileEntity> findAll();

	@GetGeneratedKeys("id")
	@SqlUpdate("INSERT INTO intercept_message_files (file_path) VALUES (:filePath)")
	long insert(@Bind("filePath") String filePath);

	@SqlUpdate("UPDATE intercept_message_files SET file_path = :filePath WHERE id = :id")
	void update(@Bind("id") long id, @Bind("filePath") String filePath);

	@SqlUpdate("DELETE FROM intercept_message_files WHERE id = :id")
	void deleteById(@Bind("id") long id);

	@DialectUpdate(provider = MessageFileAdapter.TruncateAll.class)
	void truncateAll();

	@SqlQuery("SELECT COUNT(*) > 0 FROM intercept_message_files WHERE id = :id")
	boolean existsById(@Bind("id") long id);

	@SqlQuery("SELECT COUNT(*) > 0 FROM intercept_message_files WHERE file_path = :filePath")
	boolean existsByFilePath(@Bind("filePath") String filePath);

	@SqlQuery("SELECT COUNT(*) FROM intercept_message_files")
	long count();

	/**
	 * Convenience method to save (insert or update) a file entity.
	 */
	default MessageFileEntity save(MessageFileEntity entity) {
		if (entity.getId() == null) {
			long id = insert(entity.getFilePath());
			entity.setId(id);

			return entity;
		}

		update(entity.getId(), entity.getFilePath());

		return entity;
	}
}

