package me.whereareiam.intercept.platform.direct.common.translation.defaults.type;

import me.whereareiam.configura.Config;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsPathResolver;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.DefaultsWriteContext;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.FormatDefaultsWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes default translation files in LOCALE format.
 * Creates separate files for each locale (e.g., en.yml, de.yml).
 */
final class LocaleDefaultsWriter implements FormatDefaultsWriter {
    @Override
    public List<Path> writeDefaults(DefaultsWriteContext context) {
        if (context.generatedDefaults() == null || context.generatedDefaults().isEmpty()) {
            return List.of();
        }

        DefaultsPathResolver pathResolver = context.pathResolver();
        List<Path> generated = new ArrayList<>();

        for (Map.Entry<Locale, Map<String, Object>> entry : context.generatedDefaults().entrySet()) {
            Path target = pathResolver.resolveLocaleTargetPath(
                    context.baseDirectory(),
                    context.rawPath(),
                    entry.getKey(),
                    context.defaultLocale()
            );
            if (target == null) continue;

            Path resolvedTarget = pathResolver.resolvePathWithFormat(target);
            Path existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
            if (existing != null) {
                generated.add(existing);
                continue;
            }

            if (!pathResolver.ensureParentDirectory(resolvedTarget)) {
                continue;
            }

            try {
                Config.getDefaultWriter().write(target, entry.getValue());
            } catch (Exception e) {
                // Skip this file on error; platform can handle logging if needed
                continue;
            }

            Path written = pathResolver.resolveExistingTarget(target, resolvedTarget);
            if (written != null) {
                generated.add(written);
            }
        }

        return generated;
    }
}
