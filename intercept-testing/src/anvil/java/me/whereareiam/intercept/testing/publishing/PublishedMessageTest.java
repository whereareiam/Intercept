package me.whereareiam.intercept.testing.publishing;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.intercept.testing.environment.InterceptNetwork;
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
 * A message an administrator writes into {@code translations/} on one instance and publishes reaches the other.
 */
@Tag("publishing")
class PublishedMessageTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void onPaper(ScenarioContext anvil) {
		aPublishedMessageReachesTheOtherInstance(anvil);
	}

	@Test
	@InterceptNetwork(platform = VELOCITY)
	void onVelocity(ScenarioContext anvil) {
		aPublishedMessageReachesTheOtherInstance(anvil);
	}

	@Test
	@InterceptNetwork(platform = NEOFORGE)
	void onNeoForge(ScenarioContext anvil) {
		aPublishedMessageReachesTheOtherInstance(anvil);
	}

	private static void aPublishedMessageReachesTheOtherInstance(ScenarioContext anvil) {
		Player onB = Player.join(anvil, "Alice", B);

		Administrator.on(anvil, A)
				.write(Path.of("translations", "en", "test.yml"), "news: \"Fresh news for {player}\"\n")
				.publish();

		Administrator.on(anvil, B).tell("Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}
}
