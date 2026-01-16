package me.whereareiam.intercept.platform.direct.common.persistence.templates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.path.TemplatePathResolver;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Context for writing translation template files.
 * Contains all data and utilities needed by format-specific writers.
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class TemplateWriteContext {
	private final Map<Locale, Map<String, Object>> generatedTemplates;
	private final Path baseDirectory;
	private final String rawPath;
	private final Locale defaultLocale;
	private final TemplatePathResolver pathResolver;
	private final TranslationFileCodec codec;
}
