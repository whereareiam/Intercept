package me.whereareiam.intercept.platform.direct.common.translation.defaults;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility for building and transforming translation default data structures.
 */
public final class DefaultsMapBuilder {
    private DefaultsMapBuilder() {
    }

    /**
     * Build locale-keyed defaults from raw generated data.
     * Makes defensive copies of the input maps.
     *
     * @param generated raw locale-to-values mapping
     * @return defensive copy with locale keys and value maps
     */
    public static Map<Locale, Map<String, Object>> buildLocaleDefaults(
            Map<Locale, Map<String, Object>> generated
    ) {
        if (generated == null || generated.isEmpty()) {
            return Map.of();
        }

        Map<Locale, Map<String, Object>> result = new HashMap<>();
        generated.forEach((locale, values) -> {
            Map<String, Object> converted = new HashMap<>();
            if (values != null) {
                converted.putAll(values);
            }
            result.put(locale, converted);
        });
        return result;
    }

    /**
     * Build a single default map from locale-keyed data.
     * Selects the default locale's values, or falls back to any available locale.
     *
     * @param generated     locale-to-values mapping
     * @param defaultLocale preferred locale to extract
     * @return default values map for the chosen locale
     */
    public static Map<String, Object> buildDefaultMap(
            Map<Locale, Map<String, Object>> generated,
            Locale defaultLocale
    ) {
        if (generated == null || generated.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> defaults = generated.get(defaultLocale);
        if (defaults == null) {
            defaults = generated.values().iterator().next();
        }
        if (defaults == null) {
            return Map.of();
        }

        return new HashMap<>(defaults);
    }

    /**
     * Build a multi-locale write map, restructuring from locale-first to key-first format.
     * Transforms: {en: {greeting: "Hello"}, de: {greeting: "Hallo"}}
     * Into:       {greeting: {en: "Hello", de: "Hallo"}}
     *
     * @param generated      locale-to-values mapping
     * @param formatLocale   function to format locale as string key
     * @return restructured map with keys first, then locales
     */
    public static Map<String, Object> buildMultiLocaleWriteMap(
            Map<Locale, Map<String, Object>> generated,
            java.util.function.Function<Locale, String> formatLocale
    ) {
        Map<String, Object> output = new HashMap<>();
        for (Map.Entry<Locale, Map<String, Object>> localeEntry : generated.entrySet()) {
            if (localeEntry.getKey() == null) continue;
            String localeKey = formatLocale.apply(localeEntry.getKey());
            Map<String, Object> values = localeEntry.getValue();
            if (values == null) continue;
            mergeLocaleDefaults(output, localeKey, values);
        }
        return output;
    }

    /**
     * Recursively merge locale-specific values into the output map.
     * Handles nested maps by recursing, leaf values by creating locale sub-maps.
     *
     * @param target    output map to merge into
     * @param localeKey locale identifier string
     * @param values    values to merge from this locale
     */
    private static void mergeLocaleDefaults(Map<String, Object> target, String localeKey, Map<?, ?> values) {
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                Map<String, Object> child = ensureChildMap(target, key);
                mergeLocaleDefaults(child, localeKey, nested);
            } else {
                Map<String, Object> localeMap = ensureChildMap(target, key);
                localeMap.put(localeKey, value);
            }
        }
    }

    /**
     * Ensure a child map exists at the given key, creating if necessary.
     *
     * @param target target map
     * @param key    key for child map
     * @return child map at the key
     */
    private static Map<String, Object> ensureChildMap(Map<String, Object> target, String key) {
        Object existing = target.get(key);
        if (existing instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) map;
            return cast;
        }
        Map<String, Object> created = new HashMap<>();
        target.put(key, created);
        return created;
    }
}
