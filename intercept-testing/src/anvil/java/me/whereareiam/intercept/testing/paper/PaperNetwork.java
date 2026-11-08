package me.whereareiam.intercept.testing.paper;

import me.whereareiam.anvil.api.model.process.Distribution;
import me.whereareiam.anvil.api.model.process.MinecraftServer;
import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.api.model.workspace.AssetSource;
import me.whereareiam.anvil.api.model.workspace.WorkspacePlan;
import me.whereareiam.anvil.api.scenario.AnvilScenarioDefinition;
import me.whereareiam.anvil.api.type.Platforms;
import me.whereareiam.intercept.testing.network.Network;

import java.nio.file.Path;

/**
 * Two Paper servers with Intercept and PacketEvents, sharing a database and Redis, and the probe: a plugin with a
 * translator of its own.
 */
public final class PaperNetwork implements AnvilScenarioDefinition {
	public static final String A = "paper-a";
	public static final String B = "paper-b";
	public static final Path FOLDER = Path.of("plugins", "Intercept");

	@Override
	public AnvilScenario define() {
		return AnvilScenario.builder()
				.name("paper-26.3-network")
				.entrypoint(A)
				.container(Network.postgres())
				.container(Network.redis())
				.server(server(A))
				.server(server(B))
				.build();
	}

	private static MinecraftServer server(String name) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(Network.asset("packetevents-paper", AssetSource.artifact("packetevents-paper"), Path.of("plugins", "packetevents.jar")))
				.asset(Network.asset("intercept-paper", AssetSource.artifact("intercept-paper"), Path.of("plugins", "Intercept.jar")))
				.asset(Network.asset("probe", AssetSource.artifact("probe"), Path.of("plugins", "probe.jar")));

		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.PAPER)
				.distribution(Distribution.remote("26.3", "205"))
				.workspace(Network.intercept(plan, FOLDER, name).build())
				.memoryMegabytes(1536)
				.build();
	}
}
