package me.whereareiam.intercept.translation;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * Renders keys from the network's catalog: the organisation's bundles, the shared messages and this server's local
 * messages, highest last.
 */
public interface TranslationService {
	/**
	 * Tells whether the catalog has a key in any language.
	 *
	 * @param key the key, such as {@code essentials.motd}
	 * @return whether the catalog can render it
	 */
	boolean has(@NotNull String key);

	/**
	 * Renders a key in a language, falling back through the language chain to the default language.
	 *
	 * <pre>{@code
	 * Component text = translations.render("intercept.language.chosen", Map.of("language", "Deutsch"), Locale.GERMAN);
	 * }</pre>
	 *
	 * @param key       the key
	 * @param arguments named arguments; values are strings, numbers, dates or components
	 * @param language  the language to render in
	 * @return the rendered text; the key itself when no layer has it
	 */
	@NotNull Component render(@NotNull String key, @NotNull Map<String, ?> arguments, @NotNull Locale language);

	/**
	 * Reloads every layer, including the organisation's bundles.
	 */
	void reload();
}
