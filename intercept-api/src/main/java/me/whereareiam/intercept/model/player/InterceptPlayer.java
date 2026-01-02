package me.whereareiam.intercept.model.player;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.intercept.event.player.change.PlayerInspectionModeChangedEvent;
import me.whereareiam.intercept.event.player.change.PlayerLocaleChangedEvent;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.intercept.util.EventUtil;
import me.whereareiam.keystone.Actor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

/**
 * Abstract base class for Intercept player implementations.
 * Platform-specific modules (Paper, Velocity) extend this with concrete implementations.
 */
@Getter
@ToString
public abstract class InterceptPlayer implements Actor {
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
	protected Locale locale;

	/**
	 * Whether inspection mode is enabled for this player.
	 */
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
	 * Sets the player's locale and fires a locale changed event.
	 *
	 * @param locale the new locale
	 */
	public void setLocale(@NotNull Locale locale) {
		if (this.locale.equals(locale)) return;
		
		Locale oldLocale = this.locale;
		this.locale = locale;
		EventUtil.callEvent(new PlayerLocaleChangedEvent(this, oldLocale, locale));
	}

	/**
	 * Sets the inspection mode and fires an inspection mode changed event.
	 *
	 * @param inspectionMode whether inspection mode should be enabled
	 */
	public void setInspectionMode(boolean inspectionMode) {
		if (this.inspectionMode == inspectionMode) return;
		
		boolean oldValue = this.inspectionMode;
		this.inspectionMode = inspectionMode;
		EventUtil.callEvent(new PlayerInspectionModeChangedEvent(this, oldValue, inspectionMode));
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
