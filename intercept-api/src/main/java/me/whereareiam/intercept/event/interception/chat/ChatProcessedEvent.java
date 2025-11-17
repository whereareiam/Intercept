package me.whereareiam.intercept.event.interception.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.intercept.event.interception.ProcessedEvent;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import net.kyori.adventure.text.Component;

/**
 * Event fired after a chat message has been processed by the interception system.
 * Listeners can modify the component before it's sent to the player.
 * <p>
 * This event implements {@link ProcessedEvent} to ensure modifications are
 * immediately available to the caller.
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatProcessedEvent implements ProcessedEvent {
	/**
	 * The chat interception context containing player and message information.
	 */
	private final ChatInterceptionContext context;

	@Setter
	private Component component;
}

