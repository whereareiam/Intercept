package me.whereareiam.intercept.persistence;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores the languages players chose.
 */
public interface LanguageStore {
	/**
	 * Reads a player's choice.
	 *
	 * @param uniqueId the player
	 * @return the language, or empty when they made no choice
	 */
	@NotNull Optional<Locale> find(@NotNull UUID uniqueId);

	/**
	 * Stores a player's choice, replacing an earlier one.
	 *
	 * @param uniqueId the player
	 * @param language the chosen language
	 */
	void save(@NotNull UUID uniqueId, @NotNull Locale language);

	/**
	 * Removes a player's choice.
	 *
	 * @param uniqueId the player
	 */
	void delete(@NotNull UUID uniqueId);
}
