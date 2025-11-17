package me.whereareiam.intercept.event.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.event.base.Event;
import me.whereareiam.intercept.model.player.InterceptPlayer;

/**
 * Event fired when a player is added to the PlayerRegistry.
 * This occurs when a player's data is first synced to the registry.
 */
@Getter
@RequiredArgsConstructor
public class PlayerAddedEvent implements Event {
	private final InterceptPlayer player;
}