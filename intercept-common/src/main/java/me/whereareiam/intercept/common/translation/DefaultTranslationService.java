package me.whereareiam.intercept.common.translation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.intercept.translation.TranslationService;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.Text;
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.format.adventure.TagPolicy;
import me.whereareiam.semantica.format.adventure.v5.AdventureRenderer;
import me.whereareiam.semantica.format.adventure.v5.AdventureTranslator;
import me.whereareiam.semantica.source.Source;
import me.whereareiam.semantica.source.file.FileSource;
import me.whereareiam.semantica.source.http.HttpSource;
import me.whereareiam.semantica.syntax.icu.IcuSyntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Owns the organisation's one Semantica on this instance. Its layers, lowest first: Intercept's own defaults, the
 * organisation's bundles, the shared messages from the database and this server's {@code local/messages/}.
 *
 * <p>Its translator is registered in Adventure's {@link GlobalTranslator} as the authority, so the translators that
 * plugins register for their own keys step aside whenever the network has the key.</p>
 */
@Singleton
public final class DefaultTranslationService implements TranslationService, Lifecycle {
	public static final String DEFAULTS = "defaults";
	public static final String ORGANISATION = "organisation";
	public static final String SHARED = "shared";
	public static final String LOCAL = "local";

	private static final int LOCAL_PRIORITY = Layer.OVERRIDES + 100;
	private static final String[] DEFAULT_FILES = {"en/intercept.yml"};

	private final Provider<Settings> settings;
	private final Configura configura;
	private final Path dataPath;
	private final Logger logger;
	private final AdventureRenderer renderer = AdventureRenderer.of(TagPolicy.defaults());

	private Semantica semantica;
	private Translator translator;
	private RememberingSource organisation;

	@Inject
	public DefaultTranslationService(
			@NotNull Provider<Settings> settings,
			@NotNull Configura configura,
			@Named("dataPath") @NotNull Path dataPath,
			@Named("intercept") @NotNull Logger logger
	) {
		this.settings = settings;
		this.configura = configura;
		this.dataPath = dataPath;
		this.logger = logger;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.CATALOG;
	}

	@Override
	public void start() throws IOException {
		Settings settings = this.settings.get();
		Path defaults = writeDefaults();

		Semantica.Builder builder = Semantica.builder()
				.syntax(new IcuSyntax())
				.defaultLocale(Locale.forLanguageTag(settings.getDefaultLanguage()))
				.layer(Layer.owned(DEFAULTS, FileSource.of(defaults, configura), Layer.DEFAULTS))
				.layer(Layer.owned(SHARED, new SharedSource(SharedSnapshot.EMPTY), Layer.OVERRIDES))
				.layer(Layer.owned(LOCAL, FileSource.of(dataPath.resolve("local").resolve("messages"), configura), LOCAL_PRIORITY))
				.check(renderer.check())
				.issues(issue -> logger.warning(issue.toString()))
				.watch(true);

		Settings.Organisation bundles = settings.getOrganisation();
		if (bundles.isEnabled()) {
			organisation = new RememberingSource(organisationSource(bundles));
			builder.layer(Layer.owned(ORGANISATION, organisation, Layer.SHARED));
		}

		semantica = builder.build();
		translator = AdventureTranslator.authority(semantica, renderer);
		GlobalTranslator.translator().addSource(translator);
	}

	@Override
	public void stop() {
		if (translator != null) GlobalTranslator.translator().removeSource(translator);
		if (semantica != null) semantica.close();

		translator = null;
		semantica = null;
	}

	@Override
	public boolean has(@NotNull String key) {
		return semantica().has(key);
	}

	@Override
	public @NotNull Component render(@NotNull String key, @NotNull Map<String, ?> arguments, @NotNull Locale language) {
		Text text = Text.of(key).with(arguments);
		return renderer.render(semantica().resolve(text, language));
	}

	@Override
	public void reload() {
		semantica().reload();
	}

	/**
	 * Replaces the shared layer with another revision.
	 *
	 * @param snapshot what the network shares
	 */
	public void useShared(@NotNull SharedSnapshot snapshot) {
		semantica().replace(SHARED, Layer.owned(SHARED, new SharedSource(snapshot), Layer.OVERRIDES));
	}

	/**
	 * Returns the organisation's text for a key, as last downloaded.
	 *
	 * @param language the language
	 * @param key      the key
	 * @return the text, or empty when the organisation has none or its bundles are off
	 */
	public @NotNull Optional<String> organisationText(@NotNull Locale language, @NotNull String key) {
		if (organisation == null) return Optional.empty();

		Source.Entry entry = organisation.last().entries(language).get(key);
		return Optional.ofNullable(entry).map(Source.Entry::text);
	}

	/**
	 * Returns the languages players may choose.
	 *
	 * @return the languages from {@code settings.yml}
	 */
	public @NotNull Set<Locale> languages() {
		return settings.get().getLanguages().stream()
				.map(Locale::forLanguageTag)
				.collect(Collectors.toUnmodifiableSet());
	}

	private Semantica semantica() {
		Semantica current = semantica;
		if (current == null) throw new IllegalStateException("The catalog has not started");

		return current;
	}

	private HttpSource organisationSource(Settings.Organisation bundles) {
		return HttpSource.builder(URI.create(bundles.getUrl()))
				.product(bundles.getProduct())
				.namespaces(bundles.getNamespaces().toArray(String[]::new))
				.locales(languages())
				.interval(bundles.getInterval())
				.cache(dataPath.resolve("cache").resolve("organisation"))
				.build();
	}

	// Intercept's own texts ship in the jar; they are copied out so the file source reads them like any folder.
	private Path writeDefaults() throws IOException {
		Path folder = dataPath.resolve("cache").resolve("defaults");
		for (String file : DEFAULT_FILES) {
			Path target = folder.resolve(file);
			Files.createDirectories(target.getParent());

			try (InputStream input = resource("messages/" + file)) {
				Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		return folder;
	}

	private static InputStream resource(String path) throws IOException {
		InputStream input = DefaultTranslationService.class.getClassLoader().getResourceAsStream(path);
		if (input == null) throw new IOException("The jar has no " + path);

		return input;
	}

	/**
	 * Keeps what a source loaded last, so a publication can compare a message with the organisation's text.
	 */
	private static final class RememberingSource implements Source {
		private final Source source;
		private volatile Content last = Content.empty();

		private RememberingSource(Source source) {
			this.source = source;
		}

		@Override
		public @NotNull String getName() {
			return source.getName();
		}

		@Override
		public @NotNull Content load() throws IOException {
			Content content = source.load();
			last = content;

			return content;
		}

		@Override
		public @NotNull AutoCloseable watch(@NotNull Runnable onChange) {
			return source.watch(onChange);
		}

		private @NotNull Content last() {
			return last;
		}
	}
}
