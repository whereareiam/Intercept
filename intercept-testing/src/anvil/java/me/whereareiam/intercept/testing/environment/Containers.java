package me.whereareiam.intercept.testing.environment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.anvil.api.model.process.ContainerProcess;
import me.whereareiam.intercept.testing.fixture.Bundles;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * The containers beside a test network: the database and Redis every instance shares, and the web server of the
 * remote bundles. The instances reach each one through {@code ${anvil.process.<name>.host}} and
 * {@code ${anvil.process.<name>.port}}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Containers {
	public static final String POSTGRES = "postgres";
	public static final String REDIS = "redis";
	public static final String BUNDLES = "bundles";

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
	 * A web server with the files of {@link Bundles}, as a translations repository's CI publishes them.
	 *
	 * @return the container
	 */
	public static ContainerProcess bundles() {
		StringBuilder script = new StringBuilder("mkdir -p /www");
		for (Map.Entry<String, String> file : Bundles.files().entrySet())
			script.append(" && ").append(write("/www/" + file.getKey(), file.getValue()));
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

	// The container has none of the test's files, so its command writes each one, in base64 to keep the quotes.
	private static String write(String path, String contents) {
		String encoded = Base64.getEncoder().encodeToString(contents.getBytes(StandardCharsets.UTF_8));
		String folder = path.substring(0, path.lastIndexOf('/'));

		return "mkdir -p " + folder + " && echo " + encoded + " | base64 -d > " + path;
	}
}
