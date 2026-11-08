package me.whereareiam.intercept.testing.velocity;

import me.whereareiam.anvil.api.model.process.Distribution;
import me.whereareiam.anvil.api.model.process.MinecraftProxy;
import me.whereareiam.anvil.api.model.process.MinecraftServer;
import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.api.model.workspace.AssetSource;
import me.whereareiam.anvil.api.model.workspace.WorkspacePlan;
import me.whereareiam.anvil.api.scenario.AnvilScenarioDefinition;
import me.whereareiam.anvil.api.type.Platforms;
import me.whereareiam.intercept.testing.network.Network;

import java.nio.file.Path;

/**
 * Two Velocity proxies with Intercept and PacketEvents, each in front of a Paper server of its own that runs no
 * Intercept, sharing a database and Redis.
 */
public final class VelocityNetwork implements AnvilScenarioDefinition {
	public static final String A = "velocity-a";
	public static final String B = "velocity-b";
	public static final String LOBBY_A = "lobby-a";
	public static final String LOBBY_B = "lobby-b";
	public static final Path FOLDER = Path.of("plugins", "intercept");

	@Override
	public AnvilScenario define() {
		return AnvilScenario.builder()
				.name("velocity-4.3-network")
				.entrypoint(A)
				.container(Network.postgres())
				.container(Network.redis())
				.server(lobby(LOBBY_A))
				.server(lobby(LOBBY_B))
				.proxy(proxy(A, LOBBY_A))
				.proxy(proxy(B, LOBBY_B))
				.build();
	}

	private static MinecraftServer lobby(String name) {
		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.PAPER)
				.distribution(Distribution.remote("26.3", "205"))
				.memoryMegabytes(1024)
				.build();
	}

	private static MinecraftProxy proxy(String name, String lobby) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(Network.asset("packetevents-velocity", AssetSource.artifact("packetevents-velocity"), Path.of("plugins", "packetevents.jar")))
				.asset(Network.asset("intercept-velocity", AssetSource.artifact("intercept-velocity"), Path.of("plugins", "Intercept.jar")));

		return MinecraftProxy.builder()
				.name(name)
				.platform(Platforms.VELOCITY)
				.distribution(Distribution.remote("4.3.1", "45"))
				.server(lobby)
				.defaultServer(lobby)
				.workspace(Network.intercept(plan, FOLDER, name).build())
				.build();
	}
}
