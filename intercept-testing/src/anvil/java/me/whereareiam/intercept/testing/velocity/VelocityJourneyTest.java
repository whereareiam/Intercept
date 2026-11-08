package me.whereareiam.intercept.testing.velocity;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.integration.junit.AnvilTest;
import me.whereareiam.intercept.testing.network.Journey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static me.whereareiam.intercept.testing.velocity.VelocityNetwork.A;
import static me.whereareiam.intercept.testing.velocity.VelocityNetwork.B;
import static me.whereareiam.intercept.testing.velocity.VelocityNetwork.FOLDER;
import static me.whereareiam.intercept.testing.velocity.VelocityNetwork.LOBBY_A;
import static me.whereareiam.intercept.testing.velocity.VelocityNetwork.LOBBY_B;

/**
 * Intercept on Velocity: keys rendered in the effective locale, a backend's marked system message translated as it
 * passes the proxy, and a language choice and a published message reaching the other proxy.
 */
@Tag("velocity")
final class VelocityJourneyTest {
	@Test
	@AnvilTest(VelocityNetwork.class)
	void aPlayerSeesKeysAndMarkedMessagesInTheLanguageTheyChose(ScenarioContext anvil) {
		Journey alice = Journey.join(anvil, "Alice", A);

		Journey.tell(anvil, LOBBY_A, "Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Welcome, Alice!");

		alice.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, LOBBY_A, "Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(VelocityNetwork.class)
	void aLanguageChosenOnOneProxyReachesTheOther(ScenarioContext anvil) {
		Journey onA = Journey.join(anvil, "Alice", A);
		Journey onB = Journey.join(anvil, "Alice", B);

		onA.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
		Journey.tell(anvil, LOBBY_B, "Alice", "<lang key=test.greeting player=Alice>");
		onB.sees("Willkommen, Alice!");
	}

	@Test
	@AnvilTest(VelocityNetwork.class)
	void aMessagePublishedOnOneProxyReachesTheOther(ScenarioContext anvil) {
		Journey onB = Journey.join(anvil, "Alice", B);

		Journey.write(anvil, A, FOLDER.resolve(Path.of("messages", "en", "test.yml")), "news: \"Fresh news for {player}\"\n");
		Journey.run(anvil, A, "intercept publish");

		Journey.tell(anvil, LOBBY_B, "Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}
}
