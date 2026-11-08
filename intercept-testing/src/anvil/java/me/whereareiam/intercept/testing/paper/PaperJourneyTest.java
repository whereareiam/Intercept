package me.whereareiam.intercept.testing.paper;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.integration.junit.AnvilTest;
import me.whereareiam.intercept.testing.network.Journey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static me.whereareiam.intercept.testing.paper.PaperNetwork.A;
import static me.whereareiam.intercept.testing.paper.PaperNetwork.B;
import static me.whereareiam.intercept.testing.paper.PaperNetwork.FOLDER;

/**
 * Intercept on Paper: keys rendered through Adventure's global translator, a marked system message translated on
 * its way to the player, and a language choice and a published message reaching the other server.
 */
@Tag("paper")
final class PaperJourneyTest {
	@Test
	@AnvilTest(PaperNetwork.class)
	void aPlayerSeesKeysAndMarkedMessagesInTheLanguageTheyChose(ScenarioContext anvil) {
		Journey alice = Journey.join(anvil, "Alice", A);

		Journey.tell(anvil, A, "Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Welcome, Alice!");

		alice.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, A, "Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void theNetworkAnswersBeforeAPluginsOwnTranslatorAndThePluginKeepsItsOtherKeys(ScenarioContext anvil) {
		Journey alice = Journey.join(anvil, "Alice", A);

		// Adventure keeps its translators in no order; Intercept answers first because the plugin's translator steps
		// aside for every key the network has.
		alice.command("probe").sees("Welcome, Alice!");
		alice.command("probe-own").sees("Only the probe knows Alice.");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void aLanguageChosenOnOneServerReachesTheOther(ScenarioContext anvil) {
		Journey onA = Journey.join(anvil, "Alice", A);
		Journey onB = Journey.join(anvil, "Alice", B);

		onA.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, B, "Alice", "<lang key=test.greeting player=Alice>");
		onB.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void aMessagePublishedOnOneServerReachesTheOther(ScenarioContext anvil) {
		Journey onB = Journey.join(anvil, "Alice", B);

		Journey.write(anvil, A, FOLDER.resolve(Path.of("messages", "en", "test.yml")), "news: \"Fresh news for {player}\"\n");
		Journey.run(anvil, A, "intercept publish");

		Journey.tell(anvil, B, "Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}
}
