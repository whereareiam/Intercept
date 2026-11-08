package me.whereareiam.intercept.adapter.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.semantica.format.adventure.v5.TextArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Answers a command with one of Intercept's keys and named arguments, rendered through Adventure's global translator
 * in the language the source sees. Rendering here, rather than when the host sends it, keeps a reply right in the
 * same tick a player changes their language.
 */
@Singleton
public final class Replies {
	private final LanguageService languages;

	@Inject
	public Replies(@NotNull LanguageService languages) {
		this.languages = languages;
	}

	/**
	 * Sends a key to a command's source.
	 *
	 * @param source    who ran the command
	 * @param key       the key, such as {@code intercept.language.chosen}
	 * @param arguments named arguments
	 */
	public void send(@NotNull CommandSource source, @NotNull String key, @NotNull Map<String, ?> arguments) {
		List<ComponentLike> named = new ArrayList<>();
		arguments.forEach((name, value) -> named.add(TextArgument.of(name, value)));

		UUID player = source.uniqueId();
		Locale language = player == null ? source.language() : languages.effective(player, source.language());
		source.send(GlobalTranslator.render(Component.translatable(key, named), language));
	}
}
