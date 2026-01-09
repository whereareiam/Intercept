package me.whereareiam.intercept.platform.direct.common.translation.defaults.type;

import me.whereareiam.intercept.platform.direct.common.translation.defaults.FormatDefaultsWriter;

/**
 * Factory providing type defaults writer implementations.
 * These are optional convenience implementations for common translation file patterns.
 * Platforms can use these directly, extend them, or provide completely custom writers.
 */
public final class StandardDefaultsWriters {
    /**
     * Writer for LOCALE format: separate file per locale (e.g., en.yml, de.yml).
     * Creates one file per locale with that locale's translations.
     */
    public static FormatDefaultsWriter locale() {
        return new LocaleDefaultsWriter();
    }

    /**
     * Writer for MULTI_LOCALE format: all locales in one file.
     * Structure: key -> locale -> value
     * Example:
     * <pre>
     * greeting:
     *   en: "Hello"
     *   de: "Hallo"
     * </pre>
     */
    public static FormatDefaultsWriter multiLocale() {
        return new MultiLocaleDefaultsWriter();
    }

    /**
     * Writer for TEMPLATE format: templates shared across all locales.
     * Templates contain placeholders and are language-agnostic.
     * Example:
     * <pre>
     * welcome: "Welcome, &lt;p:name&gt;!"
     * </pre>
     */
    public static FormatDefaultsWriter template() {
        return new TemplateDefaultsWriter();
    }
}
