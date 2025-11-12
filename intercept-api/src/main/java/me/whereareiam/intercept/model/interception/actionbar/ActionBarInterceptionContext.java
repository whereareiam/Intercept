package me.whereareiam.intercept.model.interception.actionbar;

import lombok.Getter;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Context object containing data extracted from an action bar component.
 */
@Getter
public class ActionBarInterceptionContext extends InterceptionContext {
	/**
	 * The original message from the action bar.
	 */
	private final Component message;

	public ActionBarInterceptionContext(UUID playerId, Locale locale, Component message) {
		super(playerId, locale);
		this.message = message;
	}
}