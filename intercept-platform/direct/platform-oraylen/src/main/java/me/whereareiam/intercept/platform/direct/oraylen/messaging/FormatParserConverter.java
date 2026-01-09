package me.whereareiam.intercept.platform.direct.oraylen.messaging;

import me.whereareiam.intercept.platform.direct.common.translation.parser.FormatParser;
import me.whereareiam.intercept.platform.direct.common.translation.parser.standard.StandardFormatParsers;
import net.oraylen.api.translation.FileFormat;

/**
 * Maps Oraylen's FileFormat enum to FormatParser implementations.
 * This is the adapter layer between Oraylen's format types and Intercept's parsing system.
 */
public final class FormatParserConverter {
    /**
     * Get FormatParser for the given Oraylen FileFormat.
     * This demonstrates the flexibility - we can map Oraylen's formats to any parser implementation.
     */
    public static FormatParser forFormat(FileFormat format) {
        if (format == null) {
            return StandardFormatParsers.locale(); // default
        }

        return switch (format) {
            case LOCALE -> StandardFormatParsers.locale();
            case MULTI_LOCALE -> StandardFormatParsers.multiLocale();
            case TEMPLATE -> StandardFormatParsers.template();
        };
    }
}
