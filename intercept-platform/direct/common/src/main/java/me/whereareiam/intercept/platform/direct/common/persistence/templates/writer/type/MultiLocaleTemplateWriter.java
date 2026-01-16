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
 * Writes translation template files in MULTI_LOCALE format.
 * Creates a single file with all locales nested under each key.
 * Structure: key -> locale -> value
 * Example:
 * <pre>
 * greeting:
 *   en: "Hello"
 *   de: "Hallo"
 * </pre>
 */
final class MultiLocaleTemplateWriter implements TemplateWriter {
    @Override
    public List<Path> writeTemplates(TemplateWriteContext context) {
        if (context.getGeneratedTemplates() == null || context.getGeneratedTemplates().isEmpty()) {
            return List.of();
        }

        TemplatePathResolver pathResolver = context.getPathResolver();
        TranslationFileCodec codec = context.getCodec();
        if (codec == null) return List.of();

        // Transform from locale-first to key-first structure
        Map<String, Object> wrapped = TemplateMapBuilder.buildMultiLocaleTemplateMap(
                context.getGeneratedTemplates(),
                pathResolver::formatLocale
        );

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
            if (!pathResolver.ensureParentDirectory(resolvedTarget)) {
                return List.of();
            }

            try {
                codec.write(resolvedTarget, wrapped);
            } catch (Exception e) {
                // Return empty on error; platform can handle logging if needed
                return List.of();
            }

            existing = pathResolver.resolveExistingTarget(target, resolvedTarget);
        }

        return existing == null ? List.of() : List.of(existing);
    }
}
