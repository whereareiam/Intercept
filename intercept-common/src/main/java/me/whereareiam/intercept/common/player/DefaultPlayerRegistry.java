package me.whereareiam.intercept.common.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.player.PlayerAddedEvent;
import me.whereareiam.intercept.event.player.PlayerDataChangedEvent;
import me.whereareiam.intercept.event.player.PlayerRemovedEvent;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of PlayerRegistry.
 * Stores player data in InterceptPlayer instances using a thread-safe ConcurrentHashMap.
 */
@Singleton
public class DefaultPlayerRegistry implements PlayerRegistry {
	private final Map<UUID, InterceptPlayer> playerDataMap = new ConcurrentHashMap<>();
	private final EventManager eventManager;

	@Inject
	public DefaultPlayerRegistry(@NotNull EventManager eventManager) {
		this.eventManager = eventManager;
		InterceptPlayer.setPlayerRegistry(this);
	}

	@Override
	@NotNull
	public Optional<InterceptPlayer> getPlayerData(@NotNull UUID playerId) {
		return Optional.ofNullable(playerDataMap.get(playerId));
	}

	@Override
	public void removePlayerData(@NotNull UUID playerId) {
		InterceptPlayer removed = playerDataMap.remove(playerId);

		if (removed != null)
			eventManager.call(new PlayerRemovedEvent(removed));
	}

	@Override
	public boolean hasPlayerData(@NotNull UUID playerId) {
		return playerDataMap.containsKey(playerId);
	}

	@Override
	public void syncPlayerData(@NotNull InterceptPlayer player) {
		InterceptPlayer stored = playerDataMap.get(player.getUniqueId());

		if (stored == null) {
			playerDataMap.put(player.getUniqueId(), player);
			eventManager.call(new PlayerAddedEvent(player));
			return;
		}

		// Track changes before updating
		boolean previousInspectionMode = stored.isInspectionMode();
		boolean newInspectionMode = player.isInspectionMode();
		boolean inspectionModeChanged = previousInspectionMode != newInspectionMode;

		// Update player data
		stored.setInspectionMode(newInspectionMode);

		// Fire event if data changed
		if (inspectionModeChanged) {
			eventManager.call(new PlayerDataChangedEvent(stored, inspectionModeChanged, previousInspectionMode));
		}
	}
}

