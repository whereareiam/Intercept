package me.whereareiam.intercept.testing.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.Map;

/**
 * The texts every test network starts with, as each instance's local messages: {@code test.greeting} in English
 * and German, and German for Intercept's own reply to {@code /language}. {@code test.news}, {@code test.link} and
 * {@code test.restart} exist nowhere until a test publishes them.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Texts {
	/**
	 * What a player sees after choosing German.
	 */
	public static final String GERMAN_CHOSEN = "Deine Sprache ist jetzt Deutsch.";

	/**
	 * The local message files, by their path inside {@code translations/local/}.
	 *
	 * @return the files
	 */
	public static Map<Path, String> local() {
		return Map.of(
				Path.of("en", "test.yml"), """
						greeting: "<gray>Welcome, <white>{player}</white>!"
						""",
				Path.of("de", "test.yml"), """
						greeting: "<gray>Willkommen, <white>{player}</white>!"
						""",
				Path.of("de", "intercept.yml"), """
						language:
						  chosen: "<gray>Deine Sprache ist jetzt <white>{language}</white>."
						"""
		);
	}
}
