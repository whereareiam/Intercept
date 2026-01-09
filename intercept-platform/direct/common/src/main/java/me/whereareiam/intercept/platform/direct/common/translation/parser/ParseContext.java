package me.whereareiam.intercept.platform.direct.common.translation.parser;

import lombok.Getter;
import me.whereareiam.intercept.platform.direct.common.util.ParsingUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Context provided to format parsers containing all necessary data and utilities.
 */
@Getter
public class ParseContext {
    private final Path root;
    private final Path file;
    private final Map<String, Object> rawData;
    private final Locale defaultLocale;
    private final ParsingUtil utilities;

    public ParseContext(
            Path root,
            Path file,
            Map<String, Object> rawData,
            Locale defaultLocale,
            ParsingUtil utilities
    ) {
        this.root = root;
        this.file = file;
        this.rawData = rawData != null ? Collections.unmodifiableMap(rawData) : Collections.emptyMap();
        this.defaultLocale = defaultLocale;
        this.utilities = utilities;
    }
}
