package me.whereareiam.intercept.event.interception.actionbar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.event.interception.ProcessedEvent;
import me.whereareiam.intercept.model.interception.actionbar.ActionBarInterceptionContext;
import net.kyori.adventure.text.Component;

/**
 * Event fired after an action bar message has been processed by the interception system.
 * Listeners can modify the component before it's sent to the player.
 * <p>
 * This event implements {@link ProcessedEvent} to ensure modifications are
 * immediately available to the caller.
 */
@Getter
@AllArgsConstructor
public class ActionBarProcessedEvent implements ProcessedEvent {
	/**
	 * The action bar interception context containing player and message information.
	 */
	private final ActionBarInterceptionContext context;

	@Setter
	private Component component;
}
