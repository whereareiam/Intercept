package me.whereareiam.intercept.platform.direct.common.translation.loader;

import com.google.inject.Provider;
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
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DirectTranslationLoader {
	private final Provider<Locale> defaultLocaleProvider;
	private final DirectMessageFileLoader fileLoader;
	private final MessageFormatRegistry formatRegistry;
	private final ReservedKeyRegistry reservedKeyRegistry;
	private final TemplateWriterResolver templateWriterResolver;
	private final TranslationFileCodecRegistry codecRegistry;
	private final TranslationFileCodecResolver codecResolver;

	public DirectTranslationLoader(
			Provider<Locale> defaultLocaleProvider,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver,
			TemplateWriterResolver templateWriterResolver
	) {
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.codecRegistry = codecRegistry;
		this.codecResolver = codecResolver;
		this.fileLoader = new DirectMessageFileLoader();
		this.templateWriterResolver = templateWriterResolver != null
				? templateWriterResolver
				: new StandardTemplateWriterResolver();
	}

	public List<MessageFileData> load(Path baseDirectory, List<DirectTranslationSourceEntry> sources) {
		return load(null, baseDirectory, sources);
	}

	public List<MessageFileData> load(
			String namespace,
			Path baseDirectory,
			List<DirectTranslationSourceEntry> sources
	) {
		if (sources == null || sources.isEmpty()) return List.of();

		Locale defaultLocale = defaultLocaleProvider.get();
		List<DirectTranslationSourceEntry> orderedSources = new ArrayList<>(sources);
		orderedSources.sort(Comparator.<DirectTranslationSourceEntry>comparingInt(
				entry -> specificity(namespace, entry, defaultLocale)
		).reversed());

		List<MessageFileData> documents = new ArrayList<>();

		for (DirectTranslationSourceEntry entry : orderedSources) {
			if (entry == null) continue;
			documents.addAll(loadSource(namespace, baseDirectory, entry, defaultLocale));
		}

		return documents;
	}

	private List<MessageFileData> loadSource(
			String namespace,
			Path baseDirectory,
			DirectTranslationSourceEntry source,
			Locale defaultLocale
	) {
		if (source == null || source.getPath() == null || source.getPath().isBlank()) {
			return List.of();
		}

		TranslationFileCodec codec = resolveCodec(namespace, source);
		DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(resolveExtensions(codec));
		DirectTranslationSourceResolver sourceResolver = new DirectTranslationSourceResolver(pathResolver);
		TemplatePathResolver templatePathResolver = new StandardTemplatePathResolver(pathResolver);
		DirectTranslationSourceResolver.ResolvedSource resolved =
				sourceResolver.resolve(baseDirectory, source.getPath(), defaultLocale);
		List<Path> files = resolved.files();

		if (files.isEmpty()) {
			files = generateTemplates(resolved, source, defaultLocale, templatePathResolver, codec);
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
					codec,
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
			Locale defaultLocale,
			TemplatePathResolver templatePathResolver,
			TranslationFileCodec codec
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
				templatePathResolver,
				codec
		);

		TemplateWriter writer = templateWriterResolver.resolve(source.getFormatId());
		if (writer == null) return List.of();

		return writer.writeTemplates(context);
	}

	private int specificity(String namespace, DirectTranslationSourceEntry entry, Locale defaultLocale) {
		if (entry == null) return 0;
		TranslationFileCodec codec = resolveCodec(namespace, entry);
		DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(resolveExtensions(codec));
		DirectTranslationSourceResolver resolver = new DirectTranslationSourceResolver(pathResolver);
		return resolver.specificity(entry.getPath(), defaultLocale);
	}

	private TranslationFileCodec resolveCodec(String namespace, DirectTranslationSourceEntry source) {
		String fileType = source == null ? null : source.getFileType();
		String path = source == null ? null : source.getPath();
		if (codecResolver != null) {
			TranslationFileCodec resolved = codecResolver.resolve(namespace, path, fileType);
			if (resolved != null) return resolved;
		}
		if (codecRegistry != null && fileType != null && !fileType.isBlank()) {
			TranslationFileCodec resolved = codecRegistry.resolveById(fileType, namespace).orElse(null);
			if (resolved != null) return resolved;
		}
		return codecRegistry == null ? null : codecRegistry.getDefault();
	}

	private List<String> resolveExtensions(TranslationFileCodec codec) {
		if (codec != null && codec.getFileExtensions() != null && !codec.getFileExtensions().isEmpty()) {
			return codec.getFileExtensions();
		}
		TranslationFileCodec fallback = codecRegistry == null ? null : codecRegistry.getDefault();
		if (fallback != null && fallback.getFileExtensions() != null && !fallback.getFileExtensions().isEmpty()) {
			return fallback.getFileExtensions();
		}
		return List.of(".yml");
	}
}
