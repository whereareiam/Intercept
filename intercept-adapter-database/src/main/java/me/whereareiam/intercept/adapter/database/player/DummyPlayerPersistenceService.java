package me.whereareiam.intercept.adapter.database.player;

import com.google.inject.Singleton;
import me.whereareiam.intercept.database.PlayerPersistenceService;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;

import java.util.Optional;
import java.util.UUID;

/**
 * Dummy implementation of PlayerPersistenceService used when persistence is disabled.
 * All operations are silent no-ops.
 */
@Singleton
public class DummyPlayerPersistenceService implements PlayerPersistenceService {
	@Override
	public Optional<PlayerData> loadPlayer(UUID playerId) {
		return Optional.empty();
	}

	@Override
	public void savePlayer(InterceptPlayer player) {
		// No-op
	}

	@Override
	public void deletePlayer(UUID playerId) {
		// No-op
	}
}