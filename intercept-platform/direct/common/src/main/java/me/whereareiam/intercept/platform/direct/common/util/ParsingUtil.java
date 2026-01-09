package me.whereareiam.intercept.platform.direct.common.util;

import me.whereareiam.intercept.Constants;
import me.whereareiam.semantica.model.TextValue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reusable utilities for parsing translation files.
 * Platforms can use these helpers when implementing their FormatParser.
 */
public final class ParsingUtil {
    /**
     * Flatten nested map structure into dot-notation keys.
     * Example: {a: {b: "value"}} -> {"a.b": "value"}
     */
    public Map<String, TextValue> flattenToTextValues(Map<String, Object> content) {
        if (content == null || content.isEmpty())
            return Map.of();

        Map<String, TextValue> result = new HashMap<>();
        flattenMap("", content, result);

        return result;
    }

    /**
     * Flatten nested map for multi-locale format.
     * Detects locale keys and organizes by key -> locale -> value.
     */
    public Map<String, Map<Locale, TextValue>> flattenMultiLocale(
            Map<String, Object> content,
            Locale defaultLocale
    ) {
        if (content == null || content.isEmpty())
            return Map.of();

        Map<String, Map<Locale, TextValue>> result = new HashMap<>();
        flattenMultiLocaleMap("", content, result, defaultLocale);

        return result;
    }

    /**
     * Parse locale token (e.g., "en", "en_US", "en-GB") into Locale object.
     */
    public Locale parseLocaleToken(String token, Locale defaultLocale) {
        if (token == null || token.isBlank())
            return null;

        if ("default".equalsIgnoreCase(token))
            return defaultLocale;

        String normalized = token.replace('-', '_');
        String[] parts = normalized.split("_", 3);
        if (parts.length == 0)
            return null;

        String language = parts[0];
        if (!isLanguageToken(language))
            return null;

        String country = parts.length > 1 ? parts[1] : "";
        String variant = parts.length > 2 ? parts[2] : "";

        return new Locale(language, country, variant);
    }

    /**
     * Detect locale from file path.
     * Looks for locale tokens in path segments (e.g., "en.yml" or "locales/en/messages.yml").
     *
     * @return LocaleMatch containing locale and segment index, or null if not found
     */
    public LocaleMatch detectLocaleFromPath(Path root, Path file, Locale defaultLocale) {
        if (root == null || file == null)
            return null;

        Path relative = root.relativize(file);
        List<String> parts = new ArrayList<>();
        for (Path part : relative)
            parts.add(part.toString());

        if (parts.isEmpty())
            return null;

        // Try filename first (without extension)
        String fileName = stripExtension(parts.get(parts.size() - 1));
        Locale localeFromFile = parseLocaleToken(fileName, defaultLocale);
        if (localeFromFile != null) {
            return new LocaleMatch(localeFromFile, parts.size() - 1);
        }

        // Try directory names
        for (int i = parts.size() - 2; i >= 0; i--) {
            Locale locale = parseLocaleToken(parts.get(i), defaultLocale);
            if (locale != null) {
                return new LocaleMatch(locale, i);
            }
        }

        return null;
    }

    /**
     * Build key prefix from file path.
     * Example: "errors/permissions.yml" -> "errors.permissions"
     */
    public String buildKeyPrefix(Path root, Path file) {
        if (root == null || file == null)
            return "";

        Path relative = root.relativize(file);
        String path = relative.toString();
        String withoutExt = stripExtension(path);
        return withoutExt.replace('\\', '.').replace('/', '.');
    }

    /**
     * Build key prefix excluding locale segment.
     * Used for LOCALE format to exclude the locale part from the key.
     */
    public String buildKeyPrefixExcludingLocale(Path root, Path file, int localeSegmentIndex) {
        if (root == null || file == null)
            return "";

        Path relative = root.relativize(file);
        List<String> parts = new ArrayList<>();
        for (Path part : relative)
            parts.add(part.toString());

        if (parts.isEmpty())
            return "";

        List<String> prefixParts = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            if (i == localeSegmentIndex)
                continue; // Skip locale segment

            String segment = parts.get(i);
            if (i == parts.size() - 1)
                segment = stripExtension(segment);

            if (!segment.isBlank())
                prefixParts.add(segment);
        }

        return String.join(".", prefixParts);
    }

    /**
     * Apply prefix to key, handling namespaced keys.
     */
    public String applyPrefix(String prefix, String key) {
        if (key == null || key.isBlank())
            return prefix != null ? prefix : "";

        if (hasNamespace(key))
            return key; // Don't prefix namespaced keys

        if (prefix == null || prefix.isBlank())
            return key;

        return prefix + "." + key;
    }

    // Private helper methods

    private void flattenMap(
            String prefix,
            Map<String, Object> map,
            Map<String, TextValue> result
    ) {
        map.forEach((key, value) -> {
            if (value == null) return;
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map<?, ?> nested) {
                Map<String, Object> nestedMap = new HashMap<>();
                nested.forEach((nestedKey, nestedValue) ->
                        nestedMap.put(String.valueOf(nestedKey), nestedValue));
                flattenMap(fullKey, nestedMap, result);
                return;
            }

            result.put(fullKey, TextValue.from(value));
        });
    }

    private void flattenMultiLocaleMap(
            String prefix,
            Map<String, Object> map,
            Map<String, Map<Locale, TextValue>> result,
            Locale defaultLocale
    ) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;

            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map<?, ?> nested) {
                Map<String, Object> nestedMap = new HashMap<>();
                nested.forEach((nestedKey, nestedValue) ->
                        nestedMap.put(String.valueOf(nestedKey), nestedValue));

                // Try to parse as locale map
                Map<Locale, TextValue> localeMap = parseLocaleMap(nestedMap, defaultLocale);
                if (localeMap != null) {
                    result.put(fullKey, localeMap);
                    continue;
                }

                // Not a locale map, recurse
                flattenMultiLocaleMap(fullKey, nestedMap, result, defaultLocale);
                continue;
            }

            // Leaf value, use default locale
            result.put(fullKey, Map.of(defaultLocale, TextValue.from(value)));
        }
    }

    private Map<Locale, TextValue> parseLocaleMap(Map<String, Object> map, Locale defaultLocale) {
        Map<Locale, TextValue> locales = new HashMap<>();
        boolean hasLocale = false;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Locale locale = parseLocaleToken(entry.getKey(), defaultLocale);
            if (locale == null) continue;
            hasLocale = true;
            Object value = entry.getValue();
            if (value == null) continue;
            locales.put(locale, TextValue.from(value));
        }

        return hasLocale ? locales : null;
    }

    private String stripExtension(String name) {
        if (name == null) return "";
        int index = name.lastIndexOf('.');
        if (index <= 0) return name;
        return name.substring(0, index);
    }

    private boolean isLanguageToken(String token) {
        if (token == null) return false;
        if (token.length() < 2 || token.length() > 3) return false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!Character.isLetter(c)) return false;
        }

        return true;
    }

    private boolean hasNamespace(String key) {
        return key != null && key.contains(Constants.Namespace.NAMESPACE_SEPARATOR);
    }

    /**
     * Result of locale detection from file path.
     */
    public record LocaleMatch(Locale locale, int segmentIndex) {
    }
}
