package me.whereareiam.intercept.testing.translation;

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
import static me.whereareiam.intercept.testing.environment.RemoteBundles.DIGESTS;
import static me.whereareiam.intercept.testing.environment.RemoteBundles.PATHS;

/**
 * Texts from the remote bundles of {@link me.whereareiam.intercept.testing.fixture.Bundles}, downloaded from a
 * web server, in the language the player chose: as a translations repository's CI writes them, and as a file depot
 * serves them by their digest.
 */
@Tag("translation")
class RemoteBundleTest {
	@Test
	@InterceptNetwork(platform = PAPER, remote = PATHS)
	void textsComeFromTheRemoteBundles(ScenarioContext anvil) {
		seesTheBundlesTexts(anvil);
	}

	@Test
	@InterceptNetwork(platform = NEOFORGE, remote = DIGESTS)
	void textsComeFromAFileDepot(ScenarioContext anvil) {
		seesTheBundlesTexts(anvil);
	}

	private static void seesTheBundlesTexts(ScenarioContext anvil) {
		Player alice = Player.join(anvil, "Alice", A);
		Administrator admin = Administrator.on(anvil, A);

		admin.tell("Alice", "<lang key=remote.welcome player=Alice>");
		alice.sees("Hello from the bundle, Alice!");

		alice.command("language de").sees(Texts.GERMAN_CHOSEN);
		admin.tell("Alice", "<lang key=remote.welcome player=Alice>");
		alice.sees("Hallo aus dem Bundle, Alice!");
	}
}
