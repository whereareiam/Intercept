package me.whereareiam.intercept.platform.direct.common.translation.parser.standard;

import me.whereareiam.intercept.platform.direct.common.translation.model.DocumentMetadata;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationEntry;
import me.whereareiam.intercept.platform.direct.common.translation.parser.FormatParser;
import me.whereareiam.intercept.platform.direct.common.translation.parser.ParseContext;
import me.whereareiam.intercept.platform.direct.common.util.ParsingUtil;
import me.whereareiam.semantica.model.TextValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for LOCALE format: separate file per locale (e.g., en.yml, de.yml).
 * Detects locale from file path and associates all entries with that locale.
 */
final class LocaleFormatParser implements FormatParser {

    @Override
    public TranslationDocument parse(ParseContext context) {
        ParsingUtil utils = context.getUtilities();

        // Detect locale from path
        ParsingUtil.LocaleMatch match = utils.detectLocaleFromPath(
                context.getRoot(),
                context.getFile(),
                context.getDefaultLocale()
        );

        if (match == null)
            // No locale detected, return empty document
            return TranslationDocument.builder()
                    .metadata(DocumentMetadata.of(context.getFile(), "LOCALE"))
                    .build();

        // Build prefix excluding locale segment
        String prefix = utils.buildKeyPrefixExcludingLocale(
                context.getRoot(),
                context.getFile(),
                match.segmentIndex()
        );

        // Flatten and create entries
        Map<String, TextValue> flattened = utils.flattenToTextValues(context.getRawData());
        Map<String, TranslationEntry> entries = new HashMap<>();

        flattened.forEach((key, value) -> {
            String fullKey = utils.applyPrefix(prefix, key);
            entries.put(fullKey, TranslationEntry.localized(fullKey, match.locale(), value));
        });

        return TranslationDocument.builder()
                .entries(entries)
                .detectedLocale(match.locale())
                .metadata(DocumentMetadata.of(context.getFile(), "LOCALE"))
                .build();
    }
}
