package me.whereareiam.intercept.adapter.database.player;

import com.google.inject.Inject;
import com.google.inject.Provider;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.database.PlayerPersistenceService;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.player.PlayerAddedEvent;
import me.whereareiam.intercept.event.player.change.PlayerInspectionModeChangedEvent;
import me.whereareiam.intercept.event.player.change.PlayerLocaleChangedEvent;
import me.whereareiam.intercept.event.player.PlayerRemovedEvent;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;

/**
 * Bridges player lifecycle events with database persistence.
 * Listens to player events and synchronizes data between memory and database.
 */
public class PlayerDatabaseBridge implements EventListener {
	private final PlayerPersistenceService persistenceService;
	private final Provider<DatabaseService> databaseServiceProvider;

	@Inject
	public PlayerDatabaseBridge(
			PlayerPersistenceService persistenceService,
			Provider<DatabaseService> databaseServiceProvider,
			EventManager eventManager
	) {
		this.persistenceService = persistenceService;
		this.databaseServiceProvider = databaseServiceProvider;
		eventManager.register(this);
	}

	/**
	 * Handle player added event - load data from database and apply to memory.
	 * Uses LOWEST order to run before other listeners see the player.
	 */
	@IntercepticEvent(EventOrder.LOWEST)
	public void onPlayerAdded(PlayerAddedEvent event) {
		if (!databaseServiceProvider.get().isInitialized()) return;

		InterceptPlayer player = event.getPlayer();
		Logger.debug("Loading player data from database for %s", player.getUniqueId());

		persistenceService.loadPlayer(player.getUniqueId())
				.ifPresent(data -> applyToPlayer(player, data));
	}

	/**
	 * Handle player removed event - save data to database before cleanup.
	 */
	@IntercepticEvent
	public void onPlayerRemoved(PlayerRemovedEvent event) {
		if (!databaseServiceProvider.get().isInitialized()) return;

		InterceptPlayer player = event.getPlayer();
		Logger.debug("Saving player data to database for %s", player.getUniqueId());

		persistenceService.savePlayer(player);
	}

	/**
	 * Handle player inspection mode changed event - persist changes immediately.
	 */
	@IntercepticEvent
	public void onInspectionModeChanged(PlayerInspectionModeChangedEvent event) {
		if (!databaseServiceProvider.get().isInitialized()) return;

		InterceptPlayer player = event.getPlayer();
		Logger.debug("Persisting inspection mode change for %s (%s -> %s)",
				player.getUniqueId(), event.isOldValue(), event.isNewValue());

		persistenceService.savePlayer(player);
	}

	/**
	 * Handle player locale changed event - persist changes immediately.
	 */
	@IntercepticEvent
	public void onLocaleChanged(PlayerLocaleChangedEvent event) {
		if (!databaseServiceProvider.get().isInitialized()) return;

		InterceptPlayer player = event.getPlayer();
		Logger.debug("Persisting locale change for %s (%s -> %s)",
				player.getUniqueId(), event.getOldLocale(), event.getNewLocale());

		persistenceService.savePlayer(player);
	}

	/**
	 * Apply loaded player data to the player instance.
	 */
	private void applyToPlayer(InterceptPlayer player, PlayerData data) {
		player.setInspectionMode(data.isInspectionMode());
		player.setLocale(data.getLocale());
		Logger.debug("Applied player data from database for %s (inspection: %s, locale: %s)",
				player.getUniqueId(), data.isInspectionMode(), data.getLocale());
	}
}