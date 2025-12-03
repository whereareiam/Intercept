package me.whereareiam.intercept.database;

import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for persisting and retrieving player data from the database.
 * Handles load and save operations for player preferences and state.
 */
public interface PlayerPersistenceService {
	/**
	 * Load player data from the database.
	 *
	 * @param playerId the player's unique identifier
	 * @return optional containing player data if found, empty otherwise
	 */
	Optional<PlayerData> loadPlayer(UUID playerId);

	/**
	 * Save player data to the database.
	 * Creates a new record if the player doesn't exist, updates if they do.
	 *
	 * @param player the player whose data should be saved
	 */
	void savePlayer(InterceptPlayer player);

	/**
	 * Delete player data from the database.
	 * Useful for cleanup or GDPR compliance.
	 *
	 * @param playerId the player's unique identifier
	 */
	void deletePlayer(UUID playerId);
}