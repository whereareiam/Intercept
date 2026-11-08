package me.whereareiam.intercept.interception.library;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interception.MessageInterceptor;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.language.LanguageService;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * What every packet library does with an outgoing system message: find the receiver's language and let the
 * interceptor translate it. A failure leaves the message as it was, so a broken rule never drops a message.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class OutgoingMessages {
	private final @NotNull MessageInterceptor interceptor;
	private final @NotNull LanguageService languages;
	private final @Named("intercept") @NotNull Logger logger;

	/**
	 * Translates one outgoing message.
	 *
	 * @param surface  where it is shown
	 * @param message  the message
	 * @param receiver the player, or {@code null} before the connection has a profile
	 * @param client   the language the player's client reports
	 * @return the translated message, or {@code null} to send the message unchanged
	 */
	public @Nullable Component rewrite(
			@NotNull Surface surface,
			@NotNull Component message,
			@Nullable UUID receiver,
			@NotNull Locale client
	) {
		Locale language = receiver == null ? client : languages.effective(receiver, client);
		try {
			return interceptor.intercept(surface, message, language);
		} catch (RuntimeException failure) {
			logger.log(Level.WARNING, "Cannot translate a " + surface + " message; sending it unchanged", failure);
			return null;
		}
	}

	/**
	 * Returns the language a player sees.
	 *
	 * @param receiver the player
	 * @param client   the language the player's client reports
	 * @return the chosen language, or the client's
	 */
	public @NotNull Locale language(@Nullable UUID receiver, @NotNull Locale client) {
		return receiver == null ? client : languages.effective(receiver, client);
	}
}
