package me.whereareiam.intercept.model.interception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Base class for all interception contexts.
 * Contains common fields shared across all component types.
 */
@Getter
@RequiredArgsConstructor
public abstract class InterceptionContext {
	/**
	 * The UUID of the player associated with this interception.
	 */
	private final UUID playerId;

	/**
	 * The locale of the player.
	 * Used for resolving messages in the player's preferred language.
	 */
	private final Locale locale;

	/**
	 * Gets the message component from this context.
	 *
	 * @return The message component
	 */
	public abstract Component getMessage();
}