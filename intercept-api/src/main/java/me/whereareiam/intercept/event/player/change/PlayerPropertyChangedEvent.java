package me.whereareiam.intercept.event.player.change;

import me.whereareiam.intercept.event.base.Event;
import me.whereareiam.intercept.model.player.InterceptPlayer;

/**
 * Base interface for player property change events.
 * Provides common access to the player whose property changed.
 */
public interface PlayerPropertyChangedEvent extends Event {
	/**
	 * Gets the player whose property changed.
	 *
	 * @return the player
	 */
	InterceptPlayer getPlayer();
}