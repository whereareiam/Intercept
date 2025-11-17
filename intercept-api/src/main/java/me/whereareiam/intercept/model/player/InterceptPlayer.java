package me.whereareiam.intercept.model.player;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.keystone.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

/**
 * Abstract base class for Intercept player implementations.
 * Platform-specific modules (Paper, Velocity) extend this with concrete implementations.
 * <p>
 * Implements Player (Keystone) which extends Actor.
 */
@Getter
@ToString
public abstract class InterceptPlayer implements Player {
	/**
	 * The player's unique identifier
	 */
	@NotNull
	protected final UUID uniqueId;

	/**
	 * The player's username
	 */
	@NotNull
	protected final String username;

	/**
	 * The player's preferred locale
	 */
	@NotNull
	protected final Locale locale;

	/**
	 * Whether inspection mode is enabled for this player.
	 */
	@Setter
	@Getter
	private boolean inspectionMode = false;

	/**
	 * Static reference to PlayerRegistry for syncing data.
	 * Set by the service implementation during initialization.
	 */
	@Setter
	private static PlayerRegistry playerRegistry;

	/**
	 * Constructor for platform-specific implementations.
	 *
	 * @param uniqueId The player's UUID
	 * @param username The player's username
	 * @param locale   The player's locale
	 */
	protected InterceptPlayer(
			@NotNull UUID uniqueId,
			@NotNull String username,
			@NotNull Locale locale
	) {
		this.uniqueId = uniqueId;
		this.username = username;
		this.locale = locale;

		if (playerRegistry != null) playerRegistry.syncPlayerData(this);
	}

	/**
	 * Sends a message to this player.
	 * Platform-specific implementation required.
	 *
	 * @param message The message to send
	 */
	@Override
	public abstract void sendMessage(@NotNull Component message);

	/**
	 * Checks if this player has a specific permission.
	 * Platform-specific implementation required.
	 *
	 * @param permission The permission to check
	 * @return true if the player has the permission
	 */
	@Override
	public abstract boolean hasPermission(@NotNull String permission);
}