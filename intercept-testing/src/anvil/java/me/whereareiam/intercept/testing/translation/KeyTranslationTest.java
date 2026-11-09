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

/**
 * A translatable component the game sends is rendered in the player's language. On NeoForge Intercept renders it
 * as the server sends the packet.
 */
@Tag("translation")
class KeyTranslationTest {
	@Test
	@InterceptNetwork(platform = NEOFORGE)
	void aTranslatableComponentIsRenderedAsItIsSent(ScenarioContext anvil) {
		Player alice = Player.join(anvil, "Alice", A);

		alice.command("language de").sees(Texts.GERMAN_CHOSEN);
		Administrator.on(anvil, A).console("tellraw Alice {\"translate\":\"test.greeting\",\"with\":[\"Alice\"]}");
		alice.sees("Willkommen,");
	}
}
