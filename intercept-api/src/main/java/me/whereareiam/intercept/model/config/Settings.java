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
	private @NotNull Organisation organisation = new Organisation();

	/**
	 * The organisation's translation bundles, built from its translations repository.
	 */
	@Getter
	@Setter
	@ToString
	public static class Organisation {
		@Comment("Whether to download the organisation's bundles.")
		private boolean enabled;
		@Comment("Where the bundles and their manifest.json are served.")
		private @NotNull String url = "https://translations.oraylen.net/";
		@Comment("The product whose bundles this server uses.")
		private @NotNull String product = "game";
		@Comment("The key namespaces to keep, such as oraylen or essentials; empty keeps all.")
		private @NotNull List<String> namespaces = new ArrayList<>();
		@Comment("How often to look for new bundles.")
		private @NotNull Duration interval = Duration.ofMinutes(5);
	}
}
