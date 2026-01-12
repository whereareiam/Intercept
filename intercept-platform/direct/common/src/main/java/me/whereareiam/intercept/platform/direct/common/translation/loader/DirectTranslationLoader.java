package me.whereareiam.intercept.platform.direct.common.translation.loader;

import com.google.inject.Provider;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectMessageFileLoader;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationPathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationSourceResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateMapBuilder;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateWriteContext;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.path.StandardTemplatePathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.path.TemplatePathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.StandardTemplateWriterResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.TemplateWriter;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.TemplateWriterResolver;
import me.whereareiam.intercept.platform.direct.common.translation.source.DirectDefaultsProvider;
import me.whereareiam.intercept.platform.direct.common.translation.source.DirectTranslationSourceEntry;
import me.whereareiam.intercept.registry.MessageFormatRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DirectTranslationLoader {
	private final Provider<Locale> defaultLocaleProvider;
	private final DirectTranslationSourceResolver sourceResolver;
	private final TemplatePathResolver templatePathResolver;
	private final DirectMessageFileLoader fileLoader;
	private final MessageFormatRegistry formatRegistry;
	private final ReservedKeyRegistry reservedKeyRegistry;
	private final TemplateWriterResolver templateWriterResolver;

	public DirectTranslationLoader(
			Provider<Locale> defaultLocaleProvider,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			TemplateWriterResolver templateWriterResolver
	) {
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.fileLoader = new DirectMessageFileLoader();
		this.templateWriterResolver = templateWriterResolver != null
				? templateWriterResolver
				: new StandardTemplateWriterResolver();

		Format configFormat = Config.getDefaultReader().getFormat();
		DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(configFormat);
		this.sourceResolver = new DirectTranslationSourceResolver(pathResolver, configFormat);
		this.templatePathResolver = new StandardTemplatePathResolver(pathResolver);
	}

	public List<MessageFileData> load(Path baseDirectory, List<DirectTranslationSourceEntry> sources) {
		if (sources == null || sources.isEmpty()) return List.of();

		Locale defaultLocale = defaultLocaleProvider.get();
		List<DirectTranslationSourceEntry> orderedSources = new ArrayList<>(sources);
		orderedSources.sort(Comparator.<DirectTranslationSourceEntry>comparingInt(
				entry -> sourceResolver.specificity(entry == null ? null : entry.getPath(), defaultLocale)
		).reversed());

		List<MessageFileData> documents = new ArrayList<>();

		for (DirectTranslationSourceEntry entry : orderedSources) {
			if (entry == null) continue;
			documents.addAll(loadSource(baseDirectory, entry, defaultLocale));
		}

		return documents;
	}

	private List<MessageFileData> loadSource(
			Path baseDirectory,
			DirectTranslationSourceEntry source,
			Locale defaultLocale
	) {
		if (source == null || source.getPath() == null || source.getPath().isBlank()) {
			return List.of();
		}

		DirectTranslationSourceResolver.ResolvedSource resolved =
				sourceResolver.resolve(baseDirectory, source.getPath(), defaultLocale);
		List<Path> files = resolved.files();

		if (files.isEmpty()) {
			files = generateTemplates(resolved, source, defaultLocale);
		}

		if (files.isEmpty()) {
			if (!source.isOptional()) {
				Logger.warn("Translation source not found: {}", resolved.rawPath());
			}
			return List.of();
		}

		List<MessageFileData> documents = new ArrayList<>();
		for (Path file : files) {
			MessageFileData document = fileLoader.load(
					resolved.root(),
					file,
					source.getFormatId(),
					source.isMultiLocale(),
					defaultLocale,
					formatRegistry,
					reservedKeyRegistry
			);
			if (document != null) {
				documents.add(document);
			}
		}

		return documents;
	}

	private List<Path> generateTemplates(
			DirectTranslationSourceResolver.ResolvedSource resolved,
			DirectTranslationSourceEntry source,
			Locale defaultLocale
	) {
		DirectDefaultsProvider defaultsProvider = source.getDefaultsProvider();
		if (defaultsProvider == null) return List.of();

		Map<Locale, Map<String, Object>> generatedTemplates = defaultsProvider.generate();
		Map<Locale, Map<String, Object>> normalized = TemplateMapBuilder.buildLocaleTemplates(generatedTemplates);
		if (normalized.isEmpty()) return List.of();

		TemplateWriteContext context = new TemplateWriteContext(
				normalized,
				resolved.baseDirectory(),
				source.getPath(),
				defaultLocale,
				templatePathResolver
		);

		TemplateWriter writer = templateWriterResolver.resolve(source.getFormatId());
		if (writer == null) return List.of();

		return writer.writeTemplates(context);
	}
}
