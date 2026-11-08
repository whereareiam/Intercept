package me.whereareiam.intercept.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The contents of {@code settings.yml}.
 */
@Getter
@Setter
@ToString
public class Settings {
	@Comment("A name for this instance in logs; empty uses a random one.")
	private @NotNull String instance = "";
	@Comment("The language of players who choose none and whose client's language has no translation.")
	private @NotNull String defaultLanguage = "en";
	@Comment("The languages players may choose, as language tags.")
	private @NotNull List<String> languages = new ArrayList<>(List.of("en"));
	@Comment("How often to check the database for messages another server published, in case replication misses it.")
	private @NotNull Duration revisionPoll = Duration.ofSeconds(30);
	private @NotNull Remote remote = new Remote();

	/**
	 * Translation bundles downloaded over HTTP, such as those an organisation's translations repository builds.
	 * They rank above Intercept's and the plugins' own texts and below {@code messages/} and {@code local/messages/}.
	 */
	@Getter
	@Setter
	@ToString
	public static class Remote {
		@Comment("Whether to download bundles; it needs a url and a product.")
		private boolean enabled;
		@Comment("Where the bundles and their manifest.json are served.")
		private @NotNull String url = "";
		@Comment("The product whose bundles this server uses, such as the name of its game mode.")
		private @NotNull String product = "";
		@Comment("The key prefixes to keep, such as game or lobby; empty keeps all.")
		private @NotNull List<String> prefixes = new ArrayList<>();
		@Comment("How often to look for new bundles.")
		private @NotNull Duration interval = Duration.ofMinutes(5);
	}
}
