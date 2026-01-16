package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.type;

import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateMapBuilder;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateWriteContext;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.path.TemplatePathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.TemplateWriter;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes translation template files in TEMPLATE format.
 * Creates a single template file with placeholders, shared across all locales.
 * Templates are language-agnostic and contain placeholder syntax.
 * Example:
 * <pre>
 * welcome: "Welcome, &lt;p:name&gt;!"
 * </pre>
 */
final class TemplateFormatWriter implements TemplateWriter {
    @Override
    public List<Path> writeTemplates(TemplateWriteContext context) {
        if (context.getGeneratedTemplates() == null || context.getGeneratedTemplates().isEmpty())
            return List.of();

        TemplatePathResolver pathResolver = context.getPathResolver();
        TranslationFileCodec codec = context.getCodec();
        if (codec == null) return List.of();

        // Extract single locale's values (preferring default locale)
        Map<String, Object> templates = TemplateMapBuilder.buildDefaultTemplateMap(
                context.getGeneratedTemplates(),
                context.getDefaultLocale()
        );
        if (templates.isEmpty()) return List.of();

        Path target = pathResolver.resolveTemplatePath(
                context.getBaseDirectory(),
                context.getRawPath(),
                "default",
                context.getDefaultLocale()
        );
        if (target == null) return List.of();

        Path resolvedTarget = pathResolver.resolvePathWithFormat(target);
        Path existing = pathResolver.resolveExistingTarget(target, resolvedTarget);

        if (existing == null) {
            if (!pathResolver.ensureParentDirectory(resolvedTarget))
                return List.of();

            try {
                codec.write(resolvedTarget, templates);
            } catch (Exception e) {
                // Return empty on error; platform can handle logging if needed
                return List.of();
            }

            existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
        }

        return existing == null
                ? List.of()
                : List.of(existing);
    }
}
