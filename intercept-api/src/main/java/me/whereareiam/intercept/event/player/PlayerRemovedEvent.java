package me.whereareiam.intercept.event.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.event.base.Event;
import me.whereareiam.intercept.model.player.InterceptPlayer;

/**
 * Event fired when a player is removed from the PlayerRegistry.
 * This typically occurs when a player disconnects from the server.
 */
@Getter
@RequiredArgsConstructor
public class PlayerRemovedEvent implements Event {
	private final InterceptPlayer player;
}