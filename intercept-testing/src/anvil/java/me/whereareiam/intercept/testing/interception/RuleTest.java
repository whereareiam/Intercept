package me.whereareiam.intercept.testing.interception;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
import me.whereareiam.intercept.testing.fixture.Texts;
import me.whereareiam.intercept.testing.player.Administrator;
import me.whereareiam.intercept.testing.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.InterceptNetwork.B;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;

/**
 * A rule published on one server translates a plugin's plain English text on the other, in the language the
 * player chose there.
 */
@Tag("interception")
class RuleTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void aPublishedRuleTranslatesPlainTextOnTheOtherServer(ScenarioContext anvil) {
		Player onB = Player.join(anvil, "Alice", B);

		Administrator.on(anvil, A)
				.write(Path.of("translations", "rules", "test.yml"), """
						restart:
						  pattern: 'Server restarts in (\\d+) minutes'
						  key: test.restart
						  arguments:
						    minutes: 1
						  in: [chat]
						  replace: whole
						""")
				.write(Path.of("translations", "de", "test.yml"), "restart: \"Neustart in {minutes} Minuten.\"\n")
				.publish();

		onB.command("language de").sees(Texts.GERMAN_CHOSEN);
		Administrator.on(anvil, B).tell("Alice", "Server restarts in 5 minutes");
		onB.sees("Neustart in 5 Minuten.");
	}
}
