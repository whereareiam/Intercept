package me.whereareiam.intercept.testing.language;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
import me.whereareiam.intercept.testing.fixture.Texts;
import me.whereareiam.intercept.testing.player.Administrator;
import me.whereareiam.intercept.testing.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.InterceptNetwork.B;
import static me.whereareiam.intercept.testing.environment.Platform.NEOFORGE;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;
import static me.whereareiam.intercept.testing.environment.Platform.VELOCITY;

/**
 * A language a player chooses on one instance applies on the other at once.
 */
@Tag("language")
class ReplicatedLanguageTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void onPaper(ScenarioContext anvil) {
		aChoiceReachesTheOtherInstance(anvil);
	}

	@Test
	@InterceptNetwork(platform = VELOCITY)
	void onVelocity(ScenarioContext anvil) {
		aChoiceReachesTheOtherInstance(anvil);
	}

	@Test
	@InterceptNetwork(platform = NEOFORGE)
	void onNeoForge(ScenarioContext anvil) {
		aChoiceReachesTheOtherInstance(anvil);
	}

	private static void aChoiceReachesTheOtherInstance(ScenarioContext anvil) {
		Player onA = Player.join(anvil, "Alice", A);
		Player onB = Player.join(anvil, "Alice", B);

		onA.command("language de").sees(Texts.GERMAN_CHOSEN);
		Administrator.on(anvil, B).tell("Alice", "<lang key=test.greeting player=Alice>");
		onB.sees("Willkommen, Alice!");
	}
}
