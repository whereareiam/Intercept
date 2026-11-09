package me.whereareiam.intercept.testing.publishing;

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
import static me.whereareiam.intercept.testing.environment.Platform.NEOFORGE;
import static me.whereareiam.intercept.testing.environment.Platform.PAPER;
import static me.whereareiam.intercept.testing.environment.Platform.VELOCITY;

/**
 * A plugin's key linked in {@code translations/all/} to the network's own key, published on one instance, is shown
 * on the other in the language the player chose there. {@code test.link} stands for the plugin's key; its text only
 * names {@code test.greeting} and passes the plugin's argument {@code name} on as {@code player}.
 */
@Tag("publishing")
class LinkedKeyTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void onPaper(ScenarioContext anvil) {
		aLinkedKeyIsShownInTheChosenLanguage(anvil);
	}

	@Test
	@InterceptNetwork(platform = VELOCITY)
	void onVelocity(ScenarioContext anvil) {
		aLinkedKeyIsShownInTheChosenLanguage(anvil);
	}

	@Test
	@InterceptNetwork(platform = NEOFORGE)
	void onNeoForge(ScenarioContext anvil) {
		aLinkedKeyIsShownInTheChosenLanguage(anvil);
	}

	private static void aLinkedKeyIsShownInTheChosenLanguage(ScenarioContext anvil) {
		Player onB = Player.join(anvil, "Alice", B);

		Administrator.on(anvil, A)
				.write(Path.of("translations", "all", "test.yml"), "link: \"{@test.greeting player=name}\"\n")
				.publish();

		onB.command("language de").sees(Texts.GERMAN_CHOSEN);
		Administrator.on(anvil, B).tell("Alice", "<lang key=test.link name=Alice>");
		onB.sees("Willkommen, Alice!");
	}
}
