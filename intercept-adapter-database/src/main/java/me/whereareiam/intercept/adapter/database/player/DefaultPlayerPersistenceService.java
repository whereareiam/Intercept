package me.whereareiam.intercept.adapter.database.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import me.whereareiam.intercept.adapter.database.repository.player.PlayerRepository;
import me.whereareiam.intercept.database.PlayerPersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of PlayerPersistenceService.
 * Handles conversion between InterceptPlayer, PlayerData, and PlayerEntity.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultPlayerPersistenceService implements PlayerPersistenceService {
	private final PlayerRepository playerRepository;
	private final Jdbi jdbi;

	@Override
	public Optional<PlayerData> loadPlayer(UUID playerId) {
		if (playerId == null) {
			Logger.warn("Attempted to load player with null UUID");
			return Optional.empty();
		}

		try {
			return jdbi.inTransaction(handle -> {
				Optional<PlayerEntity> entity = playerRepository.findByUniqueId(playerId);
				return entity.map(this::entityToData);
			});
		} catch (Exception e) {
			Logger.warn("Failed to load player data for %s: %s", playerId, e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public void savePlayer(InterceptPlayer player) {
		if (player == null) {
			Logger.warn("Attempted to save null player");
			return;
		}

		try {
			jdbi.useTransaction(handle -> {
				PlayerEntity entity = playerToEntity(player);
				playerRepository.save(entity);
			});
			Logger.debug("Saved player data for %s", player.getUniqueId());
		} catch (Exception e) {
			Logger.warn("Failed to save player data for %s: %s", player.getUniqueId(), e.getMessage());
		}
	}

	@Override
	public void deletePlayer(UUID playerId) {
		if (playerId == null) {
			Logger.warn("Attempted to delete player with null UUID");
			return;
		}

		try {
			jdbi.useTransaction(handle -> playerRepository.delete(playerId));
			Logger.debug("Deleted player data for %s", playerId);
		} catch (Exception e) {
			Logger.warn("Failed to delete player data for %s: %s", playerId, e.getMessage());
		}
	}

	/**
	 * Convert PlayerEntity to PlayerData.
	 */
	private PlayerData entityToData(PlayerEntity entity) {
		PlayerData data = new PlayerData();
		data.setUniqueId(entity.getUniqueId());
		data.setInspectionMode(entity.isInspectionMode());
		data.setLocale(entity.getLocale());

		return data;
	}

	/**
	 * Convert InterceptPlayer to PlayerEntity.
	 */
	private PlayerEntity playerToEntity(InterceptPlayer player) {
		PlayerEntity entity = new PlayerEntity();
		entity.setUniqueId(player.getUniqueId());
		entity.setInspectionMode(player.isInspectionMode());
		entity.setLocale(player.getCustomLocale());

		return entity;
	}
}