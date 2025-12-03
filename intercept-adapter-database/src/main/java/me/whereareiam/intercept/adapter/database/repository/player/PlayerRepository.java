package me.whereareiam.intercept.adapter.database.repository.player;

import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PlayerEntity operations.
 * Uses Jdbi SqlObject pattern - Jdbi automatically implements this interface.
 */
@RegisterBeanMapper(PlayerEntity.class)
public interface PlayerRepository {
	/**
	 * Find a player by their unique ID.
	 *
	 * @param uniqueId the player's UUID
	 * @return optional containing the player entity if found
	 */
	@SqlQuery("SELECT * FROM intercept_players WHERE unique_id = :uniqueId")
	Optional<PlayerEntity> findByUniqueId(@Bind("uniqueId") UUID uniqueId);

	/**
	 * Insert a new player record.
	 *
	 * @param uniqueId the player's UUID
	 * @param inspectionMode whether inspection mode is enabled
	 * @param locale the player's locale
	 */
	@SqlUpdate("INSERT INTO intercept_players (unique_id, inspection_mode, locale) VALUES (:uniqueId, :inspectionMode, :locale)")
	void insert(
			@Bind("uniqueId") UUID uniqueId,
			@Bind("inspectionMode") boolean inspectionMode,
			@Bind("locale") Locale locale
	);

	/**
	 * Update an existing player record.
	 *
	 * @param uniqueId the player's UUID
	 * @param inspectionMode whether inspection mode is enabled
	 * @param locale the player's locale
	 */
	@SqlUpdate("UPDATE intercept_players SET inspection_mode = :inspectionMode, locale = :locale WHERE unique_id = :uniqueId")
	void update(
			@Bind("uniqueId") UUID uniqueId,
			@Bind("inspectionMode") boolean inspectionMode,
			@Bind("locale") Locale locale
	);

	/**
	 * Delete a player record.
	 *
	 * @param uniqueId the player's UUID
	 */
	@SqlUpdate("DELETE FROM intercept_players WHERE unique_id = :uniqueId")
	void delete(@Bind("uniqueId") UUID uniqueId);

	/**
	 * Check if a player exists in the database.
	 *
	 * @param uniqueId the player's UUID
	 * @return true if the player exists
	 */
	@SqlQuery("SELECT COUNT(*) > 0 FROM intercept_players WHERE unique_id = :uniqueId")
	boolean exists(@Bind("uniqueId") UUID uniqueId);

	/**
	 * Save (insert or update) a player entity.
	 *
	 * @param entity the player entity to save
	 */
	default void save(PlayerEntity entity) {
		if (exists(entity.getUniqueId())) {
			update(entity.getUniqueId(), entity.isInspectionMode(), entity.getLocale());
			return;
		}

		insert(entity.getUniqueId(), entity.isInspectionMode(), entity.getLocale());
	}
}