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
	 * The player's custom locale override.
	 * If null, the player's client locale from Minecraft will be used.
	 */
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
	 */
	protected InterceptPlayer(
			@NotNull UUID uniqueId,
			@NotNull String username
	) {
		this.uniqueId = uniqueId;
		this.username = username;
		this.locale = null;

		if (playerRegistry != null) playerRegistry.syncPlayerData(this);
	}

	/**
	 * Gets the effective locale for this player.
	 * Returns the custom locale if set, otherwise returns the client's Minecraft locale.
	 *
	 * @return the effective locale to use for this player
	 */
	@NotNull
	public Locale getLocale() {
		if (locale != null)
			return locale;

		return getClientLocale();
	}

	/**
	 * Gets the player's client locale from Minecraft.
	 * Platform-specific implementation required.
	 *
	 * @return the player's client locale
	 */
	@NotNull
	public abstract Locale getClientLocale();

	/**
	 * Gets the custom locale override, if any.
	 *
	 * @return the custom locale, or null if using client locale
	 */
	public Locale getCustomLocale() {
		return locale;
	}

	/**
	 * Sets the player's custom locale override and fires a locale changed event.
	 * Set to null to use the player's client locale from Minecraft.
	 *
	 * @param locale the new custom locale, or null to use client locale
	 */
	public void setLocale(Locale locale) {
		Locale oldEffectiveLocale = getLocale();
		this.locale = locale;
		Locale newEffectiveLocale = getLocale();
		
		if (!oldEffectiveLocale.equals(newEffectiveLocale)) {
			EventUtil.callEvent(new PlayerLocaleChangedEvent(this, oldEffectiveLocale, newEffectiveLocale));
		}
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
	 * Internal method to sync player data without firing events.
	 * Used by PlayerRegistry during construction to restore state.
	 *
	 * @param inspectionMode the inspection mode to restore
	 * @param customLocale the custom locale to restore (null for client locale)
	 */
	public void syncDataFrom(boolean inspectionMode, Locale customLocale) {
		this.inspectionMode = inspectionMode;
		this.locale = customLocale;
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
