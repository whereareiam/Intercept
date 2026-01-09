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
 * Parser for TEMPLATE format: templates shared across all locales.
 * Templates contain placeholders and are language-agnostic.
 */
final class TemplateFormatParser implements FormatParser {
    @Override
    public TranslationDocument parse(ParseContext context) {
        ParsingUtil utils = context.getUtilities();

        // Build prefix from file path
        String prefix = utils.buildKeyPrefix(context.getRoot(), context.getFile());

        // Flatten and create template entries
        Map<String, TextValue> flattened = utils.flattenToTextValues(context.getRawData());
        Map<String, TranslationEntry> entries = new HashMap<>();

        flattened.forEach((key, value) -> {
            String fullKey = utils.applyPrefix(prefix, key);
            entries.put(fullKey, TranslationEntry.template(fullKey, value));
        });

        return TranslationDocument.builder()
                .entries(entries)
                .metadata(DocumentMetadata.of(context.getFile(), "TEMPLATE"))
                .build();
    }
}
