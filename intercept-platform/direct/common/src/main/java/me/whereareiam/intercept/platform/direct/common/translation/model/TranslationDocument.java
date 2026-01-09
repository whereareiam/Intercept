package me.whereareiam.intercept.platform.direct.common.translation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Locale;
import java.util.Map;

/**
 * Immutable document representing parsed translation data.
 * This is the structured output from a FormatParser.
 */
@Getter
@Builder
@AllArgsConstructor
public class TranslationDocument {
    @Singular
    private final Map<String, TranslationEntry> entries;
    private final Locale detectedLocale;
    private final DocumentMetadata metadata;
}
