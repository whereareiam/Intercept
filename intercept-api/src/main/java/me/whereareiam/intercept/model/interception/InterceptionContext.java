package me.whereareiam.intercept.model.interception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}