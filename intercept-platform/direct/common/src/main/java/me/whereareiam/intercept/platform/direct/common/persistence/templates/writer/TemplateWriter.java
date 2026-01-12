package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer;

import me.whereareiam.intercept.platform.direct.common.persistence.templates.TemplateWriteContext;

import java.nio.file.Path;
import java.util.List;

/**
 * Strategy interface for writing translation template files.
 * Platforms implement or provide instances of this interface to handle their specific formats.
 */
public interface TemplateWriter {
    /**
     * Write translation template files for the given context.
     *
     * @param context the writing context containing configuration and utilities
     * @return list of paths to generated template files
     */
    List<Path> writeTemplates(TemplateWriteContext context);
}
