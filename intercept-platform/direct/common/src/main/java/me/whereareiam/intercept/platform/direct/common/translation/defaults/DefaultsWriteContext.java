package me.whereareiam.intercept.platform.direct.common.translation.defaults;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Context for writing translation default files.
 * Contains all data and utilities needed by format-specific writers.
 *
 * @param generatedDefaults locale-to-values mapping from defaults provider
 * @param baseDirectory     base directory for resolving relative paths
 * @param rawPath           raw path configuration (may contain globs or be relative)
 * @param defaultLocale     default locale to use when locale is unspecified
 * @param pathResolver      path resolution utilities
 */
public record DefaultsWriteContext(
        Map<Locale, Map<String, Object>> generatedDefaults,
        Path baseDirectory,
        String rawPath,
        Locale defaultLocale,
        DefaultsPathResolver pathResolver
) {
}
