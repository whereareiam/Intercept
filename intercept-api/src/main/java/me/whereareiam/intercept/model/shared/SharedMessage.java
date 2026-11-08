package me.whereareiam.intercept.model.shared;

import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.Locale;

/**
 * A message the network changed from the remote text.
 */
@Value
public class SharedMessage {
	@NonNull Locale language;
	@NonNull String key;
	/**
	 * The file in {@code messages/<language>/} it is written to, such as {@code essentials.yml}.
	 */
	@NonNull String file;
	@NonNull String text;
	@NonNull String author;
	@NonNull Instant changedAt;
}
