package me.whereareiam.intercept.adapter.database.repository.message;

import me.whereareiam.intercept.adapter.database.entity.message.MessageRegexPlaceholderEntity;
import me.whereareiam.intercept.adapter.database.statement.DialectUpdate;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * Repository for MessageRegexPlaceholderEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 * Note: Table names use default values. If table names are customized, they must match the default.
 */
@RegisterBeanMapper(MessageRegexPlaceholderEntity.class)
public interface MessageRegexPlaceholderRepository {
	@SqlQuery("SELECT * FROM intercept_message_regex_placeholders WHERE pattern_id = :patternId")
	List<MessageRegexPlaceholderEntity> findAllByPatternId(@Bind("patternId") long patternId);

	@SqlUpdate("INSERT INTO intercept_message_regex_placeholders (pattern_id, placeholder_name, capture_group) VALUES (:patternId, :placeholderName, :captureGroup)")
	@GetGeneratedKeys("id")
	long insert(@Bind("patternId") long patternId, @Bind("placeholderName") String placeholderName, @Bind("captureGroup") String captureGroup);

	@DialectUpdate(provider = MessageRegexPlaceholderAdapter.TruncateAll.class)
	void truncateAll();
}

