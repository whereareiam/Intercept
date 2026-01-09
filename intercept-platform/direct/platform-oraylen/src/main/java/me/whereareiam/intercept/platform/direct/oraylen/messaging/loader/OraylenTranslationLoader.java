package me.whereareiam.intercept.platform.direct.oraylen.messaging.loader;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsMapBuilder;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsWriteContext;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.FormatDefaultsWriter;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.intercept.platform.direct.common.util.ParsingUtil;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.DefaultsWriterConverter;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OraylenTranslationLoader {
	private final Provider<Locale> defaultLocaleProvider;
	private final TranslationSourceResolver sourceResolver;
	private final OraylenDefaultsPathResolver defaultsPathResolver;
	private final TranslationFileLoader fileLoader;

	@Inject
	public OraylenTranslationLoader(
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider
	) {
		this.defaultLocaleProvider = defaultLocaleProvider;

		ParsingUtil utilities = new ParsingUtil();
		Format format = Config.getDefaultReader().getFormat();
		TranslationPathResolver pathResolver = new TranslationPathResolver(format, utilities);

		this.sourceResolver = new TranslationSourceResolver(pathResolver, format);
		this.defaultsPathResolver = new OraylenDefaultsPathResolver(pathResolver);
		this.fileLoader = new TranslationFileLoader(utilities);
	}

	public List<TranslationDocument> load(Path baseDirectory, TranslationSource source) {
		if (source == null) return List.of();

		Locale defaultLocale = resolveDefaultLocale();
		List<TranslationDocument> documents = new ArrayList<>();

		List<TranslationSource.Source> requiredSources = new ArrayList<>();
		List<TranslationSource.Source> customSources = new ArrayList<>();
		for (TranslationSource.Source entry : source.sources()) {
			if (entry == null) continue;
			if (entry.type() == TranslationSource.SourceType.REQUIRED) {
				requiredSources.add(entry);
				continue;
			}

			customSources.add(entry);
		}

		requiredSources.sort((a, b) -> Integer.compare(
				sourceResolver.specificity(b, defaultLocale),
				sourceResolver.specificity(a, defaultLocale)
		));
		customSources.sort((a, b) -> Integer.compare(
				sourceResolver.specificity(b, defaultLocale),
				sourceResolver.specificity(a, defaultLocale)
		));

		for (TranslationSource.Source entry : requiredSources) {
			List<TranslationDocument> loaded = loadSource(baseDirectory, entry, defaultLocale);
			documents.addAll(loaded);
		}

		for (TranslationSource.Source entry : customSources) {
			List<TranslationDocument> loaded = loadSource(baseDirectory, entry, defaultLocale);
			documents.addAll(loaded);
		}

		return documents;
	}

	private List<TranslationDocument> loadSource(Path baseDirectory, TranslationSource.Source source, Locale defaultLocale) {
		if (source == null || source.path() == null || source.path().isBlank())
			return List.of();

		TranslationSourceResolver.ResolvedSource resolved =
				sourceResolver.resolve(baseDirectory, source, defaultLocale);
		List<Path> files = resolved.files();

		if (files.isEmpty() && source.type() == TranslationSource.SourceType.REQUIRED)
			files = generateDefaults(resolved, source, defaultLocale);

		if (files.isEmpty()) {
			if (!source.optional()) {
				Logger.warn("Translation source not found: {}", resolved.rawPath());
			}
			return List.of();
		}

		List<TranslationDocument> documents = new ArrayList<>();
		for (Path file : files) {
			TranslationDocument document = fileLoader.load(resolved.root(), file, source.format(), defaultLocale);
			documents.add(document);
		}

		return documents;
	}

	private List<Path> generateDefaults(
			TranslationSourceResolver.ResolvedSource resolved,
			TranslationSource.Source source,
			Locale defaultLocale
	) {
		if (source.defaultsProvider() == null) return List.of();

		// Extract generated defaults from provider
		Map<Locale, Map<String, Object>> generatedDefaults = extractDefaults(source.defaultsProvider());
		if (generatedDefaults == null || generatedDefaults.isEmpty())
			return List.of();

		// Build context with all necessary data
		DefaultsWriteContext context = new DefaultsWriteContext(
				generatedDefaults,
				resolved.baseDirectory(),
				source.path(),
				defaultLocale,
				defaultsPathResolver
		);

		// Get writer for this format and write defaults
		FormatDefaultsWriter writer = DefaultsWriterConverter.forFormat(source.format());
		return writer.writeDefaults(context);
	}

	private Map<Locale, Map<String, Object>> extractDefaults(TranslationSource.DefaultsProvider provider) {
		if (provider == null) return Map.of();

		if (provider instanceof TranslationSource.DefaultsProvider.Direct direct) {
			Map<Locale, Map<String, Object>> generated = direct.generate();
			return DefaultsMapBuilder.buildLocaleDefaults(generated);
		}

		return Map.of();
	}

	private Locale resolveDefaultLocale() {
		Locale fallback = Locale.ENGLISH;

		if (defaultLocaleProvider == null) return fallback;
		Locale locale = defaultLocaleProvider.get();

		return locale != null ? locale : fallback;
	}
}
