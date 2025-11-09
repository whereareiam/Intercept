package me.whereareiam.intercept.model.interception.chat;

import lombok.Getter;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Context object containing data extracted from a chat component.
 */
@Getter
public class ChatInterceptionContext extends InterceptionContext {
	/**
	 * The original message from the component.
	 */
	private final Component message;

	public ChatInterceptionContext(UUID playerId, Locale locale, Component message) {
		super(playerId, locale);
		this.message = message;
	}
}