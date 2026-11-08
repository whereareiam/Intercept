package me.whereareiam.intercept.testing.neoforge;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.integration.junit.AnvilTest;
import me.whereareiam.intercept.testing.network.Journey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static me.whereareiam.intercept.testing.neoforge.NeoForgeNetwork.A;
import static me.whereareiam.intercept.testing.neoforge.NeoForgeNetwork.B;
import static me.whereareiam.intercept.testing.neoforge.NeoForgeNetwork.FOLDER;

/**
 * Intercept on NeoForge: keys rendered as the server sends them, a marked system message translated, and a language
 * choice and a published message reaching the other server.
 */
@Tag("neoforge")
final class NeoForgeJourneyTest {
	@Test
	@AnvilTest(NeoForgeNetwork.class)
	void aPlayerSeesKeysAndMarkedMessagesInTheLanguageTheyChose(ScenarioContext anvil) {
		Journey alice = Journey.join(anvil, "Alice", A);

		Journey.tell(anvil, A, "Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Welcome, Alice!");

		alice.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.run(anvil, A, "tellraw Alice {\"translate\":\"test.greeting\",\"with\":[\"Alice\"]}");
		alice.sees("Willkommen,");
	}

	@Test
	@AnvilTest(NeoForgeNetwork.class)
	void aLanguageChosenOnOneServerReachesTheOther(ScenarioContext anvil) {
		Journey onA = Journey.join(anvil, "Alice", A);
		Journey onB = Journey.join(anvil, "Alice", B);

		onA.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, B, "Alice", "<lang key=test.greeting player=Alice>");
		onB.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(NeoForgeNetwork.class)
	void aMessagePublishedOnOneServerReachesTheOther(ScenarioContext anvil) {
		Journey onB = Journey.join(anvil, "Alice", B);

		Journey.write(anvil, A, FOLDER.resolve(Path.of("translations", "en", "test.yml")), "news: \"Fresh news for {player}\"\n");
		Journey.run(anvil, A, "intercept publish");

		Journey.tell(anvil, B, "Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}
}
