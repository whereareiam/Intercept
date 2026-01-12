package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.type;

import me.whereareiam.configura.Config;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateWriteContext;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.path.TemplatePathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.TemplateWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes translation template files in LOCALE format.
 * Creates separate files for each locale (e.g., en.yml, de.yml).
 */
final class LocaleTemplateWriter implements TemplateWriter {
    @Override
    public List<Path> writeTemplates(TemplateWriteContext context) {
        if (context.getGeneratedTemplates() == null || context.getGeneratedTemplates().isEmpty()) {
            return List.of();
        }

        TemplatePathResolver pathResolver = context.getPathResolver();
        List<Path> generated = new ArrayList<>();

        for (Map.Entry<Locale, Map<String, Object>> entry : context.getGeneratedTemplates().entrySet()) {
            Path target = pathResolver.resolveLocaleTargetPath(
                    context.getBaseDirectory(),
                    context.getRawPath(),
                    entry.getKey(),
                    context.getDefaultLocale()
            );
            if (target == null) continue;

            Path resolvedTarget = pathResolver.resolvePathWithFormat(target);
            Path existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
            if (existing != null) {
                generated.add(existing);
                continue;
            }

            if (!pathResolver.ensureParentDirectory(resolvedTarget))
                continue;

            try {
                Config.getDefaultWriter().write(target, entry.getValue());
            } catch (Exception e) {
                // Skip this file on error; platform can handle logging if needed
                continue;
            }

            Path written = pathResolver.resolveExistingTarget(target, resolvedTarget);
            if (written != null) generated.add(written);
        }

        return generated;
    }
}
