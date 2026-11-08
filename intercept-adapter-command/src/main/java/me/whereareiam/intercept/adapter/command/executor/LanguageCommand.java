package me.whereareiam.intercept.adapter.command.executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.command.Replies;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.language.LanguageService;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code /language <language>} chooses the language a player sees on every server; {@code /language reset} goes back
 * to the client's language.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class LanguageCommand {
	private static final String RESET = "reset";

	private final @NotNull LanguageService languages;
	private final @NotNull Replies replies;

	/**
	 * Adds the command's argument and handler.
	 *
	 * @param builder the command
	 * @return the finished builder
	 */
	public @NotNull Command.Builder<CommandSource> build(@NotNull Command.Builder<CommandSource> builder) {
		return builder
				.required("language", StringParser.stringParser(), suggestions())
				.handler(this::execute);
	}

	private void execute(CommandContext<CommandSource> context) {
		CommandSource source = context.sender();
		UUID player = source.uniqueId();
		if (player == null) {
			replies.send(source, "intercept.command.players-only", Map.of());
			return;
		}

		String input = context.get("language");
		if (RESET.equalsIgnoreCase(input)) {
			languages.reset(player);
			replies.send(source, "intercept.language.reset", Map.of());
			return;
		}

		Locale language = Locale.forLanguageTag(input.replace('_', '-'));
		if (!languages.available().contains(language)) {
			replies.send(source, "intercept.language.unavailable", Map.of("language", input, "languages", available()));
			return;
		}

		languages.choose(player, language);
		replies.send(source, "intercept.language.chosen", Map.of("language", language.getDisplayLanguage(language)));
	}

	private String available() {
		return languages.available().stream()
				.map(Locale::toLanguageTag)
				.sorted()
				.collect(Collectors.joining(", "));
	}

	private SuggestionProvider<CommandSource> suggestions() {
		return SuggestionProvider.blocking((context, input) -> {
			var tags = languages.available().stream()
					.map(Locale::toLanguageTag)
					.sorted(Comparator.naturalOrder())
					.map(Suggestion::suggestion)
					.collect(Collectors.toList());
			tags.add(Suggestion.suggestion(RESET));

			return tags;
		});
	}
}
