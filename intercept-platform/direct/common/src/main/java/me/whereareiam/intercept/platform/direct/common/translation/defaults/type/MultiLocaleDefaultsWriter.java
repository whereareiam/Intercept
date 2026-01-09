package me.whereareiam.intercept.platform.direct.common.translation.defaults.type;

import me.whereareiam.configura.Config;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsMapBuilder;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsPathResolver;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsWriteContext;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.FormatDefaultsWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes default translation files in MULTI_LOCALE format.
 * Creates a single file with all locales nested under each key.
 * Structure: key -> locale -> value
 * Example:
 * <pre>
 * greeting:
 *   en: "Hello"
 *   de: "Hallo"
 * </pre>
 */
final class MultiLocaleDefaultsWriter implements FormatDefaultsWriter {
    @Override
    public List<Path> writeDefaults(DefaultsWriteContext context) {
        if (context.generatedDefaults() == null || context.generatedDefaults().isEmpty()) {
            return List.of();
        }

        DefaultsPathResolver pathResolver = context.pathResolver();

        // Transform from locale-first to key-first structure
        Map<String, Object> wrapped = DefaultsMapBuilder.buildMultiLocaleWriteMap(
                context.generatedDefaults(),
                pathResolver::formatLocale
        );

        Path target = pathResolver.resolveDefaultFilePath(
                context.baseDirectory(),
                context.rawPath(),
                "default",
                context.defaultLocale()
        );
        if (target == null) return List.of();

        Path resolvedTarget = pathResolver.resolvePathWithFormat(target);
        Path existing = pathResolver.resolveExistingTarget(target, resolvedTarget);

        if (existing == null) {
            if (!pathResolver.ensureParentDirectory(resolvedTarget)) {
                return List.of();
            }

            try {
                Config.getDefaultWriter().write(target, wrapped);
            } catch (Exception e) {
                // Return empty on error; platform can handle logging if needed
                return List.of();
            }

            existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
        }

        return existing == null ? List.of() : List.of(existing);
    }
}
