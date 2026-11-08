package me.whereareiam.intercept.model.shared;

import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

/**
 * A shared file of interception rules.
 */
@Value
public class SharedRule {
	/**
	 * The path inside {@code rules/}, such as {@code essentials.yml}.
	 */
	@NonNull String path;
	@NonNull String content;
	@NonNull String author;
	@NonNull Instant changedAt;
}
