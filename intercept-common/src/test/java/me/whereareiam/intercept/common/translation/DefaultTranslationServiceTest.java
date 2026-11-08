package me.whereareiam.intercept.common.translation;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.model.config.Settings;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTranslationServiceTest {
	@TempDir
	Path dataPath;

	@Test
	void aMessageForEveryLanguageResolvesItsReferencesInTheReadersLanguage() throws Exception {
		write("local/messages/all/identica.yml", "username:\n  taken: \"{@game.account.name-taken player=name}\"\n");
		write("local/messages/en/game.yml", "account:\n  name-taken: \"{player} is taken.\"\n");
		write("local/messages/de/game.yml", "account:\n  name-taken: \"{player} ist vergeben.\"\n");

		Settings settings = new Settings();
		settings.setLanguages(List.of("en", "de"));
		DefaultTranslationService translations = new DefaultTranslationService(
				() -> settings,
				Configura.builder().format(Format.YAML).build(),
				dataPath,
				Logger.getAnonymousLogger()
		);
		translations.start();
		try {
			assertTrue(translations.has("identica.username.taken"));
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
