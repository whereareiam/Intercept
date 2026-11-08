package me.whereareiam.intercept.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * {@code %intercept_language%} is the language a player sees, as a language tag; {@code %intercept_language_name%}
 * is its name in that language.
 */
@Singleton
public final class InterceptExpansion extends PlaceholderExpansion implements Lifecycle {
	private final LanguageService languages;

	@Inject
	public InterceptExpansion(@NotNull LanguageService languages) {
		this.languages = languages;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		register();
	}

	@Override
	public void stop() {
		unregister();
	}

	@Override
	public @NotNull String getIdentifier() {
		return "intercept";
	}

	@Override
	public @NotNull String getAuthor() {
		return "whereareiam";
	}

	@Override
	public @NotNull String getVersion() {
		return "1";
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String parameters) {
		if (!(player instanceof Player online)) return null;

		Locale language = languages.effective(online.getUniqueId(), online.locale());
		return switch (parameters) {
			case "language" -> language.toLanguageTag();
			case "language_name" -> language.getDisplayLanguage(language);
			default -> null;
		};
	}
}
