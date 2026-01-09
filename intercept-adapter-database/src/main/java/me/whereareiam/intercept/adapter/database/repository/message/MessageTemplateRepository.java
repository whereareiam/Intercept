package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.entity.message.MessageTemplateEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * Repository for MessageTemplateEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 * Note: Table names use default values. If table names are customized, they must match the default.
 */
@RegisterBeanMapper(MessageTemplateEntity.class)
public interface MessageTemplateRepository {
	@SqlQuery("SELECT * FROM intercept_message_templates WHERE entry_id = :entryId")
	MessageTemplateEntity findByEntryId(@Bind("entryId") long entryId);

	@GetGeneratedKeys("id")
	@SqlUpdate("INSERT INTO intercept_message_templates (entry_id, text) VALUES (:entryId, :text)")
	long insert(@Bind("entryId") long entryId, @Bind("text") String text);
}
