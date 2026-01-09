package me.whereareiam.intercept.platform.direct.common.translation.defaults;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Resolves file paths for default translation files.
 * Platform-specific implementations handle path conventions and formats.
 */
public interface DefaultsPathResolver {
    /**
     * Resolve target path for a locale-specific default file.
     *
     * @param baseDirectory base directory for relative paths
     * @param rawPath       raw path from source configuration (may contain globs)
     * @param locale        locale for this file
     * @param defaultLocale fallback locale if locale is null
     * @return resolved target path, or null if resolution fails
     */
    Path resolveLocaleTargetPath(Path baseDirectory, String rawPath, Locale locale, Locale defaultLocale);

    /**
     * Resolve target path for a default file with a fallback name.
     *
     * @param baseDirectory base directory for relative paths
     * @param rawPath       raw path from source configuration (may contain globs)
     * @param fallbackName  name to use for wildcards or directory targets
     * @param defaultLocale locale for path detection heuristics
     * @return resolved target path, or null if resolution fails
     */
    Path resolveDefaultFilePath(Path baseDirectory, String rawPath, String fallbackName, Locale defaultLocale);

    /**
     * Resolve path with appropriate file format extension.
     *
     * @param path path to resolve
     * @return path with extension added if needed
     */
    Path resolvePathWithFormat(Path path);

    /**
     * Check if a resolved path already exists, trying multiple variants.
     *
     * @param target         original target path
     * @param resolvedTarget resolved path with format extension
     * @return existing path if found, null otherwise
     */
    Path resolveExistingTarget(Path target, Path resolvedTarget);

    /**
     * Ensure the parent directory of the target path exists.
     *
     * @param target target file path
     * @return true if parent directory exists or was created successfully
     */
    boolean ensureParentDirectory(Path target);

    /**
     * Format a locale for use in file paths.
     *
     * @param locale locale to format
     * @return formatted string representation
     */
    String formatLocale(Locale locale);

    /**
     * Check if a path contains glob patterns.
     *
     * @param path path to check
     * @return true if path contains wildcards
     */
    boolean containsGlob(String path);
}
