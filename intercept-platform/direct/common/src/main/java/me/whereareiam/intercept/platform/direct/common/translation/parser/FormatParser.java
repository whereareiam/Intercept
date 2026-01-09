package me.whereareiam.intercept.platform.direct.common.translation.parser;

import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;

/**
 * Strategy interface for parsing translation files.
 * Platforms implement or provide instances of this interface to handle their specific formats.
 */
public interface FormatParser {
    /**
     * Parse raw data into a structured translation document.
     *
     * @param context the parsing context containing file data and utilities
     * @return parsed translation document
     */
    TranslationDocument parse(ParseContext context);
}
