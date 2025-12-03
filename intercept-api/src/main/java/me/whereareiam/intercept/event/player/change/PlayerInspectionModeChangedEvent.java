package me.whereareiam.intercept.event.player.change;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.player.InterceptPlayer;

/**
 * Event fired when a player's inspection mode changes.
 * Inspection mode allows players to see detailed message information.
 */
@Getter
@RequiredArgsConstructor
public class PlayerInspectionModeChangedEvent implements PlayerPropertyChangedEvent {
	/**
	 * The player whose inspection mode changed.
	 */
	private final InterceptPlayer player;

	/**
	 * The old inspection mode value.
	 */
	private final boolean oldValue;

	/**
	 * The new inspection mode value.
	 */
	private final boolean newValue;
}