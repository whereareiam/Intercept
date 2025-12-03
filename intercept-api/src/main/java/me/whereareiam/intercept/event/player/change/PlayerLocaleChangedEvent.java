package me.whereareiam.intercept.event.player.change;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Event fired when a player's locale preference changes.
 * This affects which language translations are shown to the player.
 */
@Getter
@RequiredArgsConstructor
public class PlayerLocaleChangedEvent implements PlayerPropertyChangedEvent {
	/**
	 * The player whose locale changed.
	 */
	private final InterceptPlayer player;

	/**
	 * The old locale value.
	 */
	@NotNull
	private final Locale oldLocale;

	/**
	 * The new locale value.
	 */
	@NotNull
	private final Locale newLocale;
}