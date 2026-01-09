package me.whereareiam.intercept.platform.direct.common.translation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.whereareiam.semantica.model.TextValue;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Immutable entry representing a single translation key with its values.
 */
@Getter
@AllArgsConstructor
public class TranslationEntry {
    private final String key;
    private final EntryType type;
    private final Map<Locale, TextValue> localizedValues;
    private final TextValue templateValue;

    public enum EntryType {
        LOCALIZED,
        TEMPLATE
    }

    public static TranslationEntry localized(String key, Locale locale, TextValue value) {
        return new TranslationEntry(key, EntryType.LOCALIZED, Map.of(locale, value), null);
    }

    public static TranslationEntry localized(String key, Map<Locale, TextValue> values) {
        Map<Locale, TextValue> copy = values != null ? Map.copyOf(values) : Collections.emptyMap();
        return new TranslationEntry(key, EntryType.LOCALIZED, copy, null);
    }

    public static TranslationEntry template(String key, TextValue value) {
        return new TranslationEntry(key, EntryType.TEMPLATE, Collections.emptyMap(), value);
    }
}
