package me.whereareiam.intercept.interception;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Translates a system message that another plugin sent as text: markers such as
 * {@code <lang key="essentials.motd" player="Alice">} are replaced by the rendered key, and a message matching a
 * rule becomes the rule's key.
 */
public interface MessageInterceptor {
	/**
	 * Translates one message.
	 *
	 * @param surface  where it is shown
	 * @param message  the message as the server would send it
	 * @param language the language of the player who receives it
	 * @return the translated message, or {@code null} when nothing in it is translated
	 */
	@Nullable Component intercept(@NotNull Surface surface, @NotNull Component message, @NotNull Locale language);
}
