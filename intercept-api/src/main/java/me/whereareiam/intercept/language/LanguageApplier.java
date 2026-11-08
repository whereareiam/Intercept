package me.whereareiam.intercept.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Makes a host use a player's chosen language. Each platform provides one: Velocity sets the effective locale,
 * Paper rewrites the locale its client reports, and NeoForge renders in the chosen language when it sends.
 */
public interface LanguageApplier {
	/**
	 * Applies a player's choice when they are on this server; does nothing otherwise.
	 *
	 * @param uniqueId the player
	 * @param language the chosen language, or {@code null} when the player follows their client again
	 */
	void apply(@NotNull UUID uniqueId, @Nullable Locale language);
}
