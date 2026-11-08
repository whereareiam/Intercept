package me.whereareiam.intercept.common.translation;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.Folders;
import me.whereareiam.intercept.model.config.Settings;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTranslationServiceTest {
	@TempDir
	Path dataPath;

	@Test
	void aMessageForEveryLanguageResolvesInTheReadersLanguageAndRulesAreNoLanguage() throws Exception {
		write("translations/local/all/identica.yml", "username:\n  taken: \"{@game.account.name-taken player=name}\"\n");
		write("translations/local/en/game.yml", "account:\n  name-taken: \"{player} is taken.\"\n");
		write("translations/local/de/game.yml", "account:\n  name-taken: \"{player} ist vergeben.\"\n");
		write("translations/local/rules/essentials.yml", "no-permission:\n  pattern: \"x\"\n  key: game.permission.missing\n");

		List<String> warnings = new ArrayList<>();
		Logger logger = Logger.getAnonymousLogger();
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				warnings.add(record.getMessage());
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		});

		Settings settings = new Settings();
		settings.setLanguages(List.of("en", "de"));
		DefaultTranslationService translations = new DefaultTranslationService(
				() -> settings,
				Configura.builder().format(Format.YAML).build(),
				new Folders(dataPath),
				logger
		);
		translations.start();
		try {
			assertTrue(translations.has("identica.username.taken"));
			assertEquals(List.of(), warnings);
			assertEquals("Alex ist vergeben.", plain(translations, Locale.GERMAN));
			assertEquals("Alex is taken.", plain(translations, Locale.ENGLISH));
		} finally {
			translations.stop();
		}
	}

	private void write(String path, String text) throws Exception {
		Path file = dataPath.resolve(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, text);
	}

	private static String plain(DefaultTranslationService translations, Locale language) {
		return PlainTextComponentSerializer.plainText()
				.serialize(translations.render("identica.username.taken", Map.of("name", "Alex"), language));
	}
}
