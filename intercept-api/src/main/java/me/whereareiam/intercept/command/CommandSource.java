package me.whereareiam.intercept.command;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Who runs a command: a player or the console. Each platform wraps its own senders.
 */
public interface CommandSource {
	/**
	 * Returns the player's unique id.
	 *
	 * @return the unique id, or {@code null} for the console
	 */
	@Nullable UUID uniqueId();

	/**
	 * Returns the name shown as the author of what this source publishes.
	 *
	 * @return the player's name, or {@code console}
	 */
	@NotNull String name();

	/**
	 * Returns the language the source's client reports; the console reports the default language.
	 *
	 * @return the language
	 */
	@NotNull Locale language();

	/**
	 * Tells whether the source has a permission.
	 *
	 * @param permission the permission
	 * @return whether it has it
	 */
	boolean hasPermission(@NotNull String permission);

	/**
	 * Sends a message that is already rendered.
	 *
	 * @param message the message
	 */
	void send(@NotNull Component message);
}
