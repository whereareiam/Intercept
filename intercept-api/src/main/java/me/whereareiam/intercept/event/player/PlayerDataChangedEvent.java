package me.whereareiam.intercept.event.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.event.base.Event;
import me.whereareiam.intercept.model.player.InterceptPlayer;

/**
 * Event fired when a player's data is updated in the PlayerRegistry.
 * This occurs when syncPlayerData is called for an existing player and their data changes.
 */
@Getter
@RequiredArgsConstructor
public class PlayerDataChangedEvent implements Event {
	/**
	 * The player whose data was changed (after the update).
	 */
	private final InterceptPlayer player;

	/**
	 * Whether the inspection mode changed.
	 */
	private final boolean inspectionModeChanged;

	/**
	 * The previous inspection mode value (before the change).
	 * Only meaningful if inspectionModeChanged is true.
	 */
	private final boolean previousInspectionMode;
}