package me.whereareiam.intercept.platform.direct.common.translation.parser.standard;

import me.whereareiam.intercept.platform.direct.common.translation.parser.FormatParser;

/**
 * Factory providing type format parser implementations.
 * These are optional convenience implementations for common translation file patterns.
 * Platforms can use these directly, extend them, or provide completely custom parsers.
 */
public final class StandardFormatParsers {
    /**
     * Parser for LOCALE format: separate file per locale (e.g., en.yml, de.yml).
     * Detects locale from file path and associates all entries with that locale.
     */
    public static FormatParser locale() {
        return new LocaleFormatParser();
    }

    /**
     * Parser for MULTI_LOCALE format: all locales in one file.
     * Structure: key -> locale -> value
     * Example:
     * <pre>
     * greeting:
     *   en: "Hello"
     *   de: "Hallo"
     * </pre>
     */
    public static FormatParser multiLocale() {
        return new MultiLocaleFormatParser();
    }

    /**
     * Parser for TEMPLATE format: templates shared across all locales.
     * Templates contain placeholders and are language-agnostic.
     * Example:
     * <pre>
     * welcome: "Welcome, &lt;p:name&gt;!"
     * </pre>
     */
    public static FormatParser template() {
        return new TemplateFormatParser();
    }
}
