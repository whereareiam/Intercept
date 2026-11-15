package me.whereareiam.intercept.testing.language;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
import me.whereareiam.intercept.testing.fixture.Texts;
import me.whereareiam.intercept.testing.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;

/**
 * Another plugin, the probe, reads the language a player chose through Intercept's API, from its own class loader.
 */
@Tag("language")
class PluginReadsLanguageTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void aPluginReadsTheChosenLanguage(ScenarioContext anvil) {
		Player alice = Player.join(anvil, "Alice", A);

		alice.command("probe-language").sees("Intercept reports no choice.");
		alice.command("language de").sees(Texts.GERMAN_CHOSEN);
		alice.command("probe-language").sees("Intercept reports de.");
	}
}
