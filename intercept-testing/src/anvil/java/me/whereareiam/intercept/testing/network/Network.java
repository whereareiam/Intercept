package me.whereareiam.intercept.testing.network;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.anvil.api.model.process.ContainerProcess;
import me.whereareiam.anvil.api.model.workspace.AssetSource;
import me.whereareiam.anvil.api.model.workspace.WorkspaceAsset;
import me.whereareiam.anvil.api.model.workspace.WorkspacePlan;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * What every network of the journeys shares: a PostgreSQL database and a Redis server in containers, and Intercept's
 * files on each instance, pointing at both.
 *
 * <p>Each instance has the journeys' texts as local messages: {@code test.greeting} in English and German, and
 * German for Intercept's own reply to {@code /language}. {@code test.news} exists nowhere until a journey publishes
 * it.</p>
 *
 * <p>A network may also serve remote bundles from a small web server, {@link #bundles()}: the product {@code test}
 * with {@code remote.welcome} in English and German.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Network {
	public static final String POSTGRES = "postgres";
	public static final String REDIS = "redis";
	public static final String BUNDLES = "bundles";

	/**
	 * The settings that point an instance at {@link #bundles()}.
	 */
	public static final String REMOTE = """
			remote:
			  enabled: true
			  url: "http://${anvil.process.bundles.host}:${anvil.process.bundles.port}/"
			  product: test
			""";

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
	 * A web server with the bundles of the product {@code test}, laid out as a translations repository's CI
	 * publishes them: {@code manifest.json}, {@code test/en.json} and {@code test/de.json}.
	 *
	 * @return the container
	 */
	public static ContainerProcess bundles() {
		Map<String, String> bundles = new TreeMap<>(Map.of(
				"en", "{\"remote.welcome\": \"Hello from the bundle, {player}!\"}",
				"de", "{\"remote.welcome\": \"Hallo aus dem Bundle, {player}!\"}"
		));
		String hashes = bundles.entrySet().stream()
				.map(bundle -> "\"" + bundle.getKey() + "\": \"" + sha256(bundle.getValue()) + "\"")
				.collect(Collectors.joining(", "));
		String manifest = "{\"format\": 1, \"revision\": \"1\", \"defaultLocale\": \"en\", \"bundles\": {\"test\": {" + hashes + "}}}";

		StringBuilder script = new StringBuilder("mkdir -p /www/test");
		script.append(" && ").append(file("/www/manifest.json", manifest));
		bundles.forEach((language, bundle) -> script.append(" && ").append(file("/www/test/" + language + ".json", bundle)));
		script.append(" && exec httpd -f -p 8080 -h /www");

		return ContainerProcess.builder()
				.name(BUNDLES)
				.image("busybox:1.36")
				.port(8080)
				.command("sh")
				.command("-c")
				.command(script.toString())
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
		return intercept(plan, folder, instance, "");
	}

	/**
	 * Adds Intercept's files to an instance, with more settings.
	 *
	 * @param plan     the instance's workspace
	 * @param folder   Intercept's folder on that platform
	 * @param instance the instance's name in Intercept's logs
	 * @param settings more lines of {@code settings.yml}, such as {@link #REMOTE}
	 * @return the workspace
	 */
	public static WorkspacePlan.WorkspacePlanBuilder intercept(WorkspacePlan.WorkspacePlanBuilder plan, Path folder, String instance, String settings) {
		return plan
				.asset(asset("intercept-settings-" + instance, SETTINGS.formatted(instance) + settings, folder.resolve("settings.yml")))
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

	// The container has no files of the journeys, so each is written by the command, in base64 to keep the quotes.
	private static String file(String path, String contents) {
		String encoded = Base64.getEncoder().encodeToString(contents.getBytes(StandardCharsets.UTF_8));
		return "echo " + encoded + " | base64 -d > " + path;
	}

	private static String sha256(String contents) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(contents.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static WorkspaceAsset asset(String group, String text, Path target) {
		return asset(group, AssetSource.text(text), target);
	}
}
