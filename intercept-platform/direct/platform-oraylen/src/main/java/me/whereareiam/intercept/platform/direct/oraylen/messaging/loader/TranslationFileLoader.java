package me.whereareiam.intercept.platform.direct.oraylen.messaging.loader;

import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.intercept.platform.direct.common.translation.parser.FormatParser;
import me.whereareiam.intercept.platform.direct.common.translation.parser.ParseContext;
import me.whereareiam.intercept.platform.direct.common.util.ParsingUtil;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.FormatParserConverter;
import net.oraylen.api.translation.FileFormat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
final class TranslationFileLoader {
	private final ParsingUtil utilities;

	TranslationDocument load(Path root, Path file, FileFormat format, Locale defaultLocale) {
		if (file == null || !Files.isRegularFile(file))
			return TranslationDocument.builder().build();

		// Load raw data from file
		Map<String, Object> data;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> loaded = Config.load(file, Map.class);
			data = loaded == null ? Map.of() : loaded;
		} catch (Exception e) {
			Logger.warn("Failed to load translation file {}: {}", file, e.getMessage());
			return TranslationDocument.builder().build();
		}

		if (data.isEmpty()) {
			return TranslationDocument.builder().build();
		}

		// Get parser for Oraylen's format
		FormatParser parser = FormatParserConverter.forFormat(format);

		// Parse using the strategy
		ParseContext context = new ParseContext(root, file, data, defaultLocale, utilities);
		TranslationDocument document = parser.parse(context);

		// Warn if locale detection failed for LOCALE format
		if (format == FileFormat.LOCALE && document.getDetectedLocale() == null) {
			Logger.warn("Failed to detect locale for file {}", file);
		}

		return document;
	}
}
