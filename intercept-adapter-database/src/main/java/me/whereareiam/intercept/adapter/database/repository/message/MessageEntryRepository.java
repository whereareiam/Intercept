package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.annotation.DialectUpdate;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MessageEntryEntity operations.
 */
@RegisterBeanMapper(MessageEntryEntity.class)
public interface MessageEntryRepository {
	@SqlQuery("SELECT * FROM intercept_message_entries WHERE file_id = :fileId AND entry_key = :entryKey")
	Optional<MessageEntryEntity> findByFileIdAndEntryKey(@Bind("fileId") long fileId, @Bind("entryKey") String entryKey);

	@SqlQuery("SELECT * FROM intercept_message_entries WHERE file_id = :fileId ORDER BY entry_key")
	List<MessageEntryEntity> findAllByFileId(@Bind("fileId") long fileId);

	@SqlUpdate("INSERT INTO intercept_message_entries (file_id, entry_key, entry_type) VALUES (:fileId, :entryKey, :entryType)")
	@GetGeneratedKeys("id")
	long insert(@Bind("fileId") long fileId, @Bind("entryKey") String entryKey, @Bind("entryType") String entryType);

	@SqlUpdate("UPDATE intercept_message_entries SET entry_type = :entryType WHERE id = :id")
	void update(@Bind("id") long id, @Bind("entryType") String entryType);

	@DialectUpdate(provider = MessageEntryAdapter.TruncateAll.class)
	void truncateAll();

	/**
	 * Convenience method to save (insert or update) an entry entity.
	 */
	default MessageEntryEntity save(MessageEntryEntity entity) {
		if (entity.getId() == null) {
			long fileId = entity.getFile() != null ? entity.getFile().getId() : 0;
			String entryType = entity.getEntryType() != null ? entity.getEntryType().name() : null;

			long id = insert(fileId, entity.getEntryKey(), entryType);
			entity.setId(id);

			return entity;
		}

		String entryType = entity.getEntryType() != null ? entity.getEntryType().name() : null;
		update(entity.getId(), entryType);

		return entity;
	}
}