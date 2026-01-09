package me.whereareiam.intercept.platform.direct.oraylen.messaging;

import me.whereareiam.intercept.platform.direct.common.translation.defaults.FormatDefaultsWriter;
import me.whereareiam.intercept.platform.direct.common.translation.defaults.type.StandardDefaultsWriters;
import net.oraylen.api.translation.FileFormat;

/**
 * Maps Oraylen's FileFormat enum to FormatDefaultsWriter implementations.
 * This is the adapter layer between Oraylen's format types and Intercept's defaults writing system.
 */
public final class DefaultsWriterConverter {
    /**
     * Get FormatDefaultsWriter for the given Oraylen FileFormat.
     * This demonstrates the flexibility - we can map Oraylen's formats to any writer implementation.
     */
    public static FormatDefaultsWriter forFormat(FileFormat format) {
        if (format == null) {
            return StandardDefaultsWriters.locale(); // default
        }

        return switch (format) {
            case LOCALE -> StandardDefaultsWriters.locale();
            case MULTI_LOCALE -> StandardDefaultsWriters.multiLocale();
            case TEMPLATE -> StandardDefaultsWriters.template();
        };
    }
}
