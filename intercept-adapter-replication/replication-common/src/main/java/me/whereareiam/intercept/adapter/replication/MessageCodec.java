package me.whereareiam.intercept.adapter.replication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.intercept.model.replication.LanguageChosen;
import me.whereareiam.intercept.model.replication.Replicated;
import me.whereareiam.intercept.model.replication.ReloadRequested;
import me.whereareiam.intercept.model.replication.RevisionPublished;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Writes replicated messages as one line of fields separated by spaces, starting with the sending instance and the
 * message type: {@code <instance> language <uuid> <tag or ->}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageCodec {
	private static final String NONE = "-";

	/**
	 * Encodes a message.
	 *
	 * @param instance the sending instance
	 * @param message  the message
	 * @return the line
	 */
	public static @NotNull String encode(@NotNull String instance, @NotNull Replicated message) {
		String body = switch (message) {
			case RevisionPublished published -> "revision " + published.getRevision();
			case LanguageChosen chosen -> "language " + chosen.getUniqueId() + " "
					+ (chosen.getLanguage() == null ? NONE : chosen.getLanguage().toLanguageTag());
			case ReloadRequested ignored -> "reload";
		};

		return instance + " " + body;
	}

	/**
	 * Decodes a line.
	 *
	 * @param line the line
	 * @return the sender and the message, or {@code null} for a line of an unknown type
	 */
	public static @Nullable Decoded decode(@NotNull String line) {
		String[] fields = line.split(" ");
		if (fields.length < 2) return null;

		Replicated message = switch (fields[1]) {
			case "revision" -> fields.length == 3 ? new RevisionPublished(Long.parseLong(fields[2])) : null;
			case "language" -> fields.length == 4 ? new LanguageChosen(
					UUID.fromString(fields[2]),
					NONE.equals(fields[3]) ? null : Locale.forLanguageTag(fields[3])
			) : null;
			case "reload" -> new ReloadRequested();
			default -> null;
		};

		return message == null ? null : new Decoded(fields[0], message);
	}

	/**
	 * A received message and the instance that sent it.
	 *
	 * @param instance the sender
	 * @param message  the message
	 */
	public record Decoded(@NotNull String instance, @NotNull Replicated message) {
	}
}
