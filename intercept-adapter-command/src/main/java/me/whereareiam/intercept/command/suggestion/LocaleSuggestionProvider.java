package me.whereareiam.intercept.command.suggestion;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Global suggestion provider for locale codes.
 * Determines available locales from the messaging system.
 * Can be used by any command that needs to suggest locale codes.
 */
@Singleton
public class LocaleSuggestionProvider {
	private final MessageRegistry messageRegistry;

	@Inject
	public LocaleSuggestionProvider(@NotNull MessageRegistry messageRegistry) {
		this.messageRegistry = messageRegistry;
	}

	/**
	 * Provides locale code suggestions filtered by input.
	 * Returns all locales that are available in the messaging system.
	 *
	 * @param context the command context
	 * @param input   the current input string
	 * @return list of locale code suggestions
	 */
	@Suggestions("locales")
	public @NotNull List<@NotNull Suggestion> suggestLocales(
			@NotNull CommandContext<Actor> context,
			@NotNull String input
	) {
		// Collect all unique locales from all message entries
		Set<String> availableLocales = new TreeSet<>();
		for (CompiledMessageEntry entry : messageRegistry.getAllEntries().values()) {
			if (entry.hasTranslations()) {
				for (Locale locale : entry.getLocales()) {
					String localeString = LocaleUtil.formatLocale(locale);
					if (!localeString.isEmpty()) availableLocales.add(localeString);
				}
			}
		}

		String lowerInput = input.toLowerCase();
		return availableLocales.stream()
				.filter(locale -> locale.toLowerCase().startsWith(lowerInput))
				.map(Suggestion::suggestion)
				.collect(Collectors.toList());
	}
}