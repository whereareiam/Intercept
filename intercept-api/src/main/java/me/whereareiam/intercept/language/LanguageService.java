package me.whereareiam.intercept.language;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The languages players choose. A choice is stored for the whole network, applied on every server the player is on
 * and replicated to the other instances, so a player keeps their language when they switch servers.
 *
 * <p>A player without a choice sees the language their client reports.</p>
 */
public interface LanguageService {
	/**
	 * Returns the language a player chose.
	 *
	 * @param uniqueId the player
	 * @return the chosen language, or empty when the player follows their client
	 */
	@NotNull Optional<Locale> chosen(@NotNull UUID uniqueId);

	/**
	 * Returns the language a player sees: their choice, or else the language their client reports.
	 *
	 * @param uniqueId the player
	 * @param client   the language the player's client reports
	 * @return the language to render messages in
	 */
	default @NotNull Locale effective(@NotNull UUID uniqueId, @NotNull Locale client) {
		return chosen(uniqueId).orElse(client);
	}

	/**
	 * Stores a player's choice, applies it on this server and tells the other instances.
	 *
	 * @param uniqueId the player
	 * @param language one of {@link #available()}
	 * @throws IllegalArgumentException when the language is not available
	 */
	void choose(@NotNull UUID uniqueId, @NotNull Locale language);

	/**
	 * Removes a player's choice, so they follow their client again.
	 *
	 * @param uniqueId the player
	 */
	void reset(@NotNull UUID uniqueId);

	/**
	 * Reads a player's stored choice into this instance before they join.
	 *
	 * @param uniqueId the player
	 */
	void load(@NotNull UUID uniqueId);

	/**
	 * Forgets a player who left this instance.
	 *
	 * @param uniqueId the player
	 */
	void unload(@NotNull UUID uniqueId);

	/**
	 * Returns the languages players may choose, as configured in {@code settings.yml}.
	 *
	 * @return the available languages
	 */
	@NotNull Set<Locale> available();
}
