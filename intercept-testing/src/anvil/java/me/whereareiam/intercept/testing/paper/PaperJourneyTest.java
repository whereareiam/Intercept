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
 * its way to the player, a language choice, a published message, a link in {@code all/} and a rule reaching the
 * other server, and texts from remote bundles.
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

		Journey.write(anvil, A, FOLDER.resolve(Path.of("translations", "en", "test.yml")), "news: \"Fresh news for {player}\"\n");
		Journey.run(anvil, A, "intercept publish");

		Journey.tell(anvil, B, "Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void aPluginKeyLinkedInAllIsShownInTheReadersLanguageOnTheOtherServer(ScenarioContext anvil) {
		Journey onB = Journey.join(anvil, "Alice", B);

		// test.link stands for a plugin's key: its text in all/ only names the network's own key test.greeting.
		Journey.write(anvil, A, FOLDER.resolve(Path.of("translations", "all", "test.yml")), "link: \"{@test.greeting player=name}\"\n");
		Journey.run(anvil, A, "intercept publish");

		onB.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, B, "Alice", "<lang key=test.link name=Alice>");
		onB.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void aPublishedRuleTranslatesAPluginsPlainTextOnTheOtherServer(ScenarioContext anvil) {
		Journey onB = Journey.join(anvil, "Alice", B);

		Journey.write(anvil, A, FOLDER.resolve(Path.of("translations", "rules", "test.yml")), """
				restart:
				  pattern: 'Server restarts in (\\d+) minutes'
				  key: test.restart
				  arguments:
				    minutes: 1
				  in: [chat]
				  replace: whole
				""");
		Journey.write(anvil, A, FOLDER.resolve(Path.of("translations", "de", "test.yml")), "restart: \"Neustart in {minutes} Minuten.\"\n");
		Journey.run(anvil, A, "intercept publish");

		onB.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, B, "Alice", "Server restarts in 5 minutes");
		onB.sees("Neustart in 5 Minuten.");
	}

	@Test
	@AnvilTest(PaperNetwork.class)
	void textsComeFromTheRemoteBundlesInTheLanguageTheyChose(ScenarioContext anvil) {
		Journey alice = Journey.join(anvil, "Alice", A);

		Journey.tell(anvil, A, "Alice", "<lang key=remote.welcome player=Alice>");
		alice.sees("Hello from the bundle, Alice!");

		alice.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, A, "Alice", "<lang key=remote.welcome player=Alice>");
		alice.sees("Hallo aus dem Bundle, Alice!");
	}
}
