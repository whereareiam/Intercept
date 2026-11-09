package me.whereareiam.intercept.testing.environment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.anvil.api.model.process.MinecraftServer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.api.type.Platforms;

import java.nio.file.Path;

/**
 * A platform Intercept runs on in a test network, and where its files are there.
 */
@Getter
@RequiredArgsConstructor
public enum Platform {
	PAPER(Platforms.PAPER, Path.of("plugins", "Intercept")),
	/**
	 * Velocity names Intercept's folder after the plugin's id.
	 */
	VELOCITY(Platforms.VELOCITY, Path.of("plugins", "intercept")),
	/**
	 * On NeoForge, Intercept keeps its folder in the server's directory.
	 */
	NEOFORGE(Platforms.NEOFORGE, Path.of("intercept"));

	/**
	 * Anvil's id of the platform.
	 */
	private final String id;
	/**
	 * Intercept's folder inside an instance's workspace.
	 */
	private final Path folder;

	/**
	 * Returns the platform of a running network.
	 *
	 * @param anvil the running network
	 * @return its platform
	 */
	public static Platform of(ScenarioContext anvil) {
		if (!anvil.definition().getProxies().isEmpty()) return VELOCITY;

		String id = anvil.definition().getServers().stream()
				.map(MinecraftServer::getPlatform)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("The network has no servers"));
		for (Platform platform : values())
			if (platform.id.equals(id)) return platform;

		throw new IllegalStateException("The network runs on " + id + ", which no test network builds");
	}

	/**
	 * Returns the server whose console sends system messages to the players of an instance: the instance itself,
	 * or on Velocity the Paper server behind the proxy, so the message passes the proxy as a plugin's would.
	 *
	 * @param instance {@link InterceptNetwork#A} or {@link InterceptNetwork#B}
	 * @return the server
	 */
	public String sender(String instance) {
		if (this != VELOCITY) return instance;

		return instance.equals(InterceptNetwork.A) ? InterceptNetwork.LOBBY_A : InterceptNetwork.LOBBY_B;
	}
}
