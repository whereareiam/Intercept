package me.whereareiam.intercept.testing.translation;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
import me.whereareiam.intercept.testing.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;

/**
 * A plugin with a translator of its own, the probe. Adventure asks its translators in no order, so Intercept
 * answers first only because the plugin's translator steps aside for every key the network has. The probe keeps
 * the keys only it knows.
 */
@Tag("translation")
class PluginTranslatorTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void theNetworkAnswersBeforeThePluginWhichKeepsItsOtherKeys(ScenarioContext anvil) {
		Player alice = Player.join(anvil, "Alice", A);

		alice.command("probe").sees("Welcome, Alice!");
		alice.command("probe-own").sees("Only the probe knows Alice.");
	}
}
