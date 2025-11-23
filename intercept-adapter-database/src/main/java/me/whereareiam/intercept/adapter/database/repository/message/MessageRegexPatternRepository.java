package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.entity.message.MessageRegexPatternEntity;
import me.whereareiam.intercept.adapter.database.statement.DialectUpdate;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * Repository for MessageRegexPatternEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 * Note: Table names use default values. If table names are customized, they must match the default.
 */
@RegisterBeanMapper(MessageRegexPatternEntity.class)
public interface MessageRegexPatternRepository {
	@SqlQuery("SELECT * FROM intercept_message_regex_patterns WHERE entry_id = :entryId ORDER BY sort_order")
	List<MessageRegexPatternEntity> findAllByEntryId(@Bind("entryId") long entryId);

	@SqlUpdate("INSERT INTO intercept_message_regex_patterns (entry_id, pattern, priority, replace_matched, sort_order) VALUES (:entryId, :pattern, :priority, :replaceMatched, :sortOrder)")
	@GetGeneratedKeys("id")
	long insert(@Bind("entryId") long entryId, @Bind("pattern") String pattern, @Bind("priority") int priority, @Bind("replaceMatched") boolean replaceMatched, @Bind("sortOrder") int sortOrder);

	@DialectUpdate(provider = MessageRegexPatternAdapter.TruncateAll.class)
	void truncateAll();
}

