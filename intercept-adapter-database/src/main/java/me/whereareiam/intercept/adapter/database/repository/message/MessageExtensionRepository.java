package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.annotation.DialectUpdate;
import me.whereareiam.intercept.adapter.database.entity.message.MessageExtensionEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

/**
 * Repository for MessageExtensionEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 */
@RegisterBeanMapper(MessageExtensionEntity.class)
public interface MessageExtensionRepository {
	@SqlQuery("SELECT * FROM intercept_message_extensions WHERE entry_id = :entryId ORDER BY extension_id")
	List<MessageExtensionEntity> findAllByEntryId(@Bind("entryId") long entryId);

	@DialectUpdate(provider = MessageExtensionAdapter.Insert.class)
	@GetGeneratedKeys("id")
	long insert(
			@Bind("entryId") long entryId,
			@Bind("extensionId") String extensionId,
			@Bind("payload") String payload
	);
}
