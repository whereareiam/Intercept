package me.whereareiam.intercept.testing.neoforge;

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
 * Two NeoForge servers on Minecraft 1.21.1 with Intercept, sharing a database and Redis.
 */
public final class NeoForgeNetwork implements AnvilScenarioDefinition {
	public static final String A = "neoforge-a";
	public static final String B = "neoforge-b";
	public static final Path FOLDER = Path.of("intercept");

	@Override
	public AnvilScenario define() {
		return AnvilScenario.builder()
				.name("neoforge-1.21.1-network")
				.entrypoint(A)
				.container(Network.postgres())
				.container(Network.redis())
				.server(server(A))
				.server(server(B))
				.build();
	}

	private static MinecraftServer server(String name) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(Network.asset("intercept-neoforge", AssetSource.artifact("intercept-neoforge"), Path.of("mods", "intercept.jar")));

		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.NEOFORGE)
				.distribution(Distribution.remote("1.21.1", "21.1.252"))
				.workspace(Network.intercept(plan, FOLDER, name).build())
				.memoryMegabytes(2048)
				.build();
	}
}
