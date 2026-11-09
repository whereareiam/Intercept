package me.whereareiam.intercept.testing.interception;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
import me.whereareiam.intercept.testing.fixture.Texts;
import me.whereareiam.intercept.testing.player.Administrator;
import me.whereareiam.intercept.testing.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.Platform.NEOFORGE;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;
import static me.whereareiam.intercept.testing.environment.Platform.VELOCITY;

/**
 * A plugin that writes a marker, {@code <lang key=... player=...>}, into a system message gets the key's text in
 * the language the player chose.
 */
@Tag("interception")
class MarkerTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void onPaper(ScenarioContext anvil) {
		aMarkerShowsTheChosenLanguage(anvil);
	}

	@Test
	@InterceptNetwork(platform = VELOCITY)
	void onVelocity(ScenarioContext anvil) {
		aMarkerShowsTheChosenLanguage(anvil);
	}

	@Test
	@InterceptNetwork(platform = NEOFORGE)
	void onNeoForge(ScenarioContext anvil) {
		aMarkerShowsTheChosenLanguage(anvil);
	}

	private static void aMarkerShowsTheChosenLanguage(ScenarioContext anvil) {
		Player alice = Player.join(anvil, "Alice", A);
		Administrator admin = Administrator.on(anvil, A);

		admin.tell("Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Welcome, Alice!");

		alice.command("language de").sees(Texts.GERMAN_CHOSEN);
		admin.tell("Alice", "<lang key=test.greeting player=Alice>");
		alice.sees("Willkommen, Alice!");
	}
}
