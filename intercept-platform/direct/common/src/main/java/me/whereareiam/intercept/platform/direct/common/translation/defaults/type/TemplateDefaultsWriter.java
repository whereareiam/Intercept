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
 * Writes default translation files in TEMPLATE format.
 * Creates a single template file with placeholders, shared across all locales.
 * Templates are language-agnostic and contain placeholder syntax.
 * Example:
 * <pre>
 * welcome: "Welcome, &lt;p:name&gt;!"
 * </pre>
 */
final class TemplateDefaultsWriter implements FormatDefaultsWriter {
    @Override
    public List<Path> writeDefaults(DefaultsWriteContext context) {
        if (context.generatedDefaults() == null || context.generatedDefaults().isEmpty()) {
            return List.of();
        }

        DefaultsPathResolver pathResolver = context.pathResolver();

        // Extract single locale's values (preferring default locale)
        Map<String, Object> defaults = DefaultsMapBuilder.buildDefaultMap(
                context.generatedDefaults(),
                context.defaultLocale()
        );
        if (defaults.isEmpty()) return List.of();

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
                Config.getDefaultWriter().write(target, defaults);
            } catch (Exception e) {
                // Return empty on error; platform can handle logging if needed
                return List.of();
            }

            existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
        }

        return existing == null ? List.of() : List.of(existing);
    }
}
