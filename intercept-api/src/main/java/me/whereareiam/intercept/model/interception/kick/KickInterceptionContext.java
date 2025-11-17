package me.whereareiam.intercept.model.interception.kick;

import lombok.Getter;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Context object containing data extracted from a kick/disconnect component.
 */
@Getter
public class KickInterceptionContext extends InterceptionContext {
	/**
	 * The original kick reason message from the disconnect packet.
	 */
	private final Component message;

	public KickInterceptionContext(UUID playerId, Locale locale, Component message) {
		super(playerId, locale);
		this.message = message;
	}
}

