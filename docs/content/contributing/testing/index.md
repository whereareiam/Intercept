---
title: Live tests
description: Run the scenarios on real Paper, Velocity and NeoForge servers.
---

`intercept-testing` starts the packaged plugin and mod on real servers through Anvil and drives simulated players
through them. Each scenario gets a fresh network of two instances on one platform, sharing a PostgreSQL database and
a Redis server in Docker containers, so the scenarios need Docker.

```bash
./gradlew :intercept-testing:anvil
```

Or one suite at a time:

```bash
./gradlew :intercept-testing:anvilInterceptionTest
./gradlew :intercept-testing:anvilLanguageTest
./gradlew :intercept-testing:anvilTranslationTest
./gradlew :intercept-testing:anvilPublishingTest
```

| Suite | What its scenarios show | Platforms |
| --- | --- | --- |
| `interception` | a marker in a system message shows the chosen language; a published rule translates a plugin's plain text on the other server | all; rules on Paper |
| `language` | a language chosen on one instance applies on the other at once | all |
| `translation` | a translatable component rendered as it is sent; a plugin with its own translator leaves the network's keys to Intercept and keeps its others; texts from remote bundles | NeoForge; Paper; Paper |
| `publishing` | a message published on one instance shown on the other; a plugin key linked in `translations/all/` shown in German on the other | all |

## Writing a scenario

Put it in the package of the behaviour it shows and tag the class with that suite. `ScenarioSuitesTest`, which
`anvilTest` runs, fails when a scenario is in no suite or in two.

```java
@Tag("publishing")
class PublishedMessageTest {
	@Test
	@InterceptNetwork(platform = PAPER)
	void onPaper(ScenarioContext anvil) {
		aPublishedMessageReachesTheOtherInstance(anvil);
	}

	// onVelocity and onNeoForge the same way

	private static void aPublishedMessageReachesTheOtherInstance(ScenarioContext anvil) {
		Player onB = Player.join(anvil, "Alice", B);

		Administrator.on(anvil, A)
				.write(Path.of("translations", "en", "test.yml"), "news: \"Fresh news for {player}\"\n")
				.publish();

		Administrator.on(anvil, B).tell("Alice", "<lang key=test.news player=Alice>");
		onB.sees("Fresh news for Alice");
	}
}
```

`@InterceptNetwork` takes the platform and `remote = true` to serve the remote bundles of `fixture/Bundles` from a
web server container. `Administrator.tell` sends a system message as a plugin without translations would; on
Velocity it comes from the Paper server behind the proxy, so it passes the proxy. The texts every network starts
with are in `fixture/Texts`.

Scenarios run in parallel, two at most by default; set `intercept.scenarios.parallelism` in `gradle.properties`
to change it. A failed scenario keeps its network under `intercept-testing/build/anvil/`; each process's
`anvil-console.log` tells what it did.
