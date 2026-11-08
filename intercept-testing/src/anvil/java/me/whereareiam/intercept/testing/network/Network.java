package me.whereareiam.intercept.testing.network;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.anvil.api.model.process.ContainerProcess;
import me.whereareiam.anvil.api.model.workspace.AssetSource;
import me.whereareiam.anvil.api.model.workspace.WorkspaceAsset;
import me.whereareiam.anvil.api.model.workspace.WorkspacePlan;

import java.nio.file.Path;

/**
 * What every network of the journeys shares: a PostgreSQL database and a Redis server in containers, and Intercept's
 * files on each instance, pointing at both.
 *
 * <p>Each instance has the journeys' texts as local messages: {@code test.greeting} in English and German, and
 * German for Intercept's own reply to {@code /language}. {@code test.news} exists nowhere until a journey publishes
 * it.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Network {
	public static final String POSTGRES = "postgres";
	public static final String REDIS = "redis";

	private static final String SETTINGS = """
			instance: %s
			languages: [en, de]
			revisionPoll: 2s
			""";
	private static final String PERSISTENCE = """
			type: POSTGRES
			host: "${anvil.process.postgres.host}"
			port: ${anvil.process.postgres.port}
			database: intercept
			username: intercept
			password: intercept
			""";
	private static final String REPLICATION = """
			enabled: true
			host: "${anvil.process.redis.host}"
			port: ${anvil.process.redis.port}
			""";
	private static final String ENGLISH = """
			greeting: "<gray>Welcome, <white>{player}</white>!"
			""";
	private static final String GERMAN = """
			greeting: "<gray>Willkommen, <white>{player}</white>!"
			""";
	private static final String GERMAN_INTERCEPT = """
			language:
			  chosen: "<gray>Deine Sprache ist jetzt <white>{language}</white>."
			""";

	/**
	 * The database every instance stores in.
	 *
	 * @return the container
	 */
	public static ContainerProcess postgres() {
		return ContainerProcess.builder()
				.name(POSTGRES)
				.image("postgres:17-alpine")
				.port(5432)
				.environment("POSTGRES_DB", "intercept")
				.environment("POSTGRES_USER", "intercept")
				.environment("POSTGRES_PASSWORD", "intercept")
				.executionProviderId("docker")
				.build();
	}

	/**
	 * The Redis server instances replicate through.
	 *
	 * @return the container
	 */
	public static ContainerProcess redis() {
		return ContainerProcess.builder()
				.name(REDIS)
				.image("redis:7.2-alpine")
				.port(6379)
				.readyLog("Ready to accept connections")
				.executionProviderId("docker")
				.build();
	}

	/**
	 * Adds Intercept's files to an instance.
	 *
	 * @param plan     the instance's workspace
	 * @param folder   Intercept's folder on that platform, such as {@code plugins/Intercept}
	 * @param instance the instance's name in Intercept's logs
	 * @return the workspace
	 */
	public static WorkspacePlan.WorkspacePlanBuilder intercept(WorkspacePlan.WorkspacePlanBuilder plan, Path folder, String instance) {
		return plan
				.asset(asset("intercept-settings-" + instance, SETTINGS.formatted(instance), folder.resolve("settings.yml")))
				.asset(asset("intercept-persistence", PERSISTENCE, folder.resolve("persistence.yml")))
				.asset(asset("intercept-replication", REPLICATION, folder.resolve("replication.yml")))
				.asset(asset("intercept-english", ENGLISH, folder.resolve(Path.of("translations", "local", "en", "test.yml"))))
				.asset(asset("intercept-german", GERMAN, folder.resolve(Path.of("translations", "local", "de", "test.yml"))))
				.asset(asset("intercept-german-own", GERMAN_INTERCEPT, folder.resolve(Path.of("translations", "local", "de", "intercept.yml"))));
	}

	/**
	 * A file of a workspace.
	 *
	 * @param group  the name the workspace cache knows it by
	 * @param source what it holds
	 * @param target where it goes
	 * @return the asset
	 */
	public static WorkspaceAsset asset(String group, AssetSource source, Path target) {
		return WorkspaceAsset.builder().group(group).source(source).target(target).build();
	}

	private static WorkspaceAsset asset(String group, String text, Path target) {
		return asset(group, AssetSource.text(text), target);
	}
}
