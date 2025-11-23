package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.dialectica.annotation.DialectUpdate;
import me.whereareiam.intercept.adapter.database.entity.message.MessageTranslationEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Locale;

/**
 * Repository for MessageTranslationEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 * Note: Table names use default values. If table names are customized, they must match the default.
 */
@RegisterBeanMapper(MessageTranslationEntity.class)
public interface MessageTranslationRepository {
	@SqlQuery("SELECT * FROM intercept_message_translations WHERE entry_id = :entryId ORDER BY locale")
	List<MessageTranslationEntity> findAllByEntryId(@Bind("entryId") long entryId);

	@SqlUpdate("INSERT INTO intercept_message_translations (entry_id, locale, text) VALUES (:entryId, :locale, :text)")
	@GetGeneratedKeys("id")
	long insert(@Bind("entryId") long entryId, @Bind("locale") Locale locale, @Bind("text") String text);

	@DialectUpdate(provider = MessageTranslationAdapter.TruncateAll.class)
	void truncateAll();
}