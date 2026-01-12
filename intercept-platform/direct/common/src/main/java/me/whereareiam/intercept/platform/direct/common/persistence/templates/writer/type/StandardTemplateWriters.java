package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.type;

import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.TemplateWriter;

/**
 * Factory providing standard template writer implementations.
 * Platforms can use these directly, extend them, or provide custom writers.
 */
public final class StandardTemplateWriters {
    /**
     * Writer for LOCALE format: separate file per locale (e.g., en.yml, de.yml).
     */
    public static TemplateWriter locale() {
        return new LocaleTemplateWriter();
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
    public static TemplateWriter multiLocale() {
        return new MultiLocaleTemplateWriter();
    }

    /**
     * Writer for TEMPLATE format: templates shared across all locales.
     * Example:
     * <pre>
     * welcome: "Welcome, &lt;p:name&gt;!"
     * </pre>
     */
    public static TemplateWriter templateFormat() {
        return new TemplateFormatWriter();
    }
}
