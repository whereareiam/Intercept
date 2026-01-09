package me.whereareiam.intercept.platform.direct.common.translation.defaults;

import java.nio.file.Path;
import java.util.List;

/**
 * Strategy interface for writing translation default files.
 * Platforms implement or provide instances of this interface to handle their specific formats.
 */
public interface FormatDefaultsWriter {
    /**
     * Write default translation files for the given context.
     *
     * @param context the writing context containing configuration and utilities
     * @return list of paths to generated default files
     */
    List<Path> writeDefaults(DefaultsWriteContext context);
}
