package me.whereareiam.intercept.event.interception.kick;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.intercept.event.interception.ProcessedEvent;
import me.whereareiam.intercept.model.interception.kick.KickInterceptionContext;
import net.kyori.adventure.text.Component;

/**
 * Event fired after a kick/disconnect message has been processed by the interception system.
 * Listeners can modify the component before it's sent to the player.
 * <p>
 * This event implements {@link ProcessedEvent} to ensure modifications are
 * immediately available to the caller.
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class KickProcessedEvent implements ProcessedEvent {
	/**
	 * The kick interception context containing player and message information.
	 */
	private final KickInterceptionContext context;

	@Setter
	private Component component;
}