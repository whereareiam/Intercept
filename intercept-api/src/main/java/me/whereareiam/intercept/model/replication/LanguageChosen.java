package me.whereareiam.intercept.model.replication;

import lombok.NonNull;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * A player chose a language, or reset their choice.
 */
@Value
public class LanguageChosen implements Replicated {
	@NonNull UUID uniqueId;
	/**
	 * The chosen language, or {@code null} when the player follows their client again.
	 */
	@Nullable Locale language;
}
