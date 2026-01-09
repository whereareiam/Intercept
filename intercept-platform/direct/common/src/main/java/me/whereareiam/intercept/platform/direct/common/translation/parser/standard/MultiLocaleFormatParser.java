package me.whereareiam.intercept.platform.direct.common.translation.parser.standard;

import me.whereareiam.intercept.platform.direct.common.translation.model.DocumentMetadata;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationEntry;
import me.whereareiam.intercept.platform.direct.common.translation.parser.FormatParser;
import me.whereareiam.intercept.platform.direct.common.translation.parser.ParseContext;
import me.whereareiam.intercept.platform.direct.common.util.ParsingUtil;
import me.whereareiam.semantica.model.TextValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parser for MULTI_LOCALE format: all locales in one file.
 * Structure: key -> locale -> value
 */
final class MultiLocaleFormatParser implements FormatParser {
    @Override
    public TranslationDocument parse(ParseContext context) {
        ParsingUtil utils = context.getUtilities();

        // Build prefix from file path
        String prefix = utils.buildKeyPrefix(context.getRoot(), context.getFile());

        // Flatten multi-locale structure
        Map<String, Map<Locale, TextValue>> flattened = utils.flattenMultiLocale(
                context.getRawData(),
                context.getDefaultLocale()
        );

        // Create entries
        Map<String, TranslationEntry> entries = new HashMap<>();
        flattened.forEach((key, localeMap) -> {
            String fullKey = utils.applyPrefix(prefix, key);
            entries.put(fullKey, TranslationEntry.localized(fullKey, localeMap));
        });

        return TranslationDocument.builder()
                .entries(entries)
                .metadata(DocumentMetadata.of(context.getFile(), "MULTI_LOCALE"))
                .build();
    }
}
