package me.whereareiam.intercept.common.persistence.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Default format context implementation.
 */
@Getter
@RequiredArgsConstructor
public class DefaultFormatContext implements FormatContext {
	private final Path root;
	private final Path file;
	private final Locale defaultLocale;
	private final String namespace;
	private final ReservedKeyRegistry reservedKeyRegistry;
}
