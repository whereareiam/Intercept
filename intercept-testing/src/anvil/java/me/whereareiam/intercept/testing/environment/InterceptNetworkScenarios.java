package me.whereareiam.intercept.testing.environment;

import me.whereareiam.anvil.api.model.process.Distribution;
import me.whereareiam.anvil.api.model.process.MinecraftProxy;
import me.whereareiam.anvil.api.model.process.MinecraftServer;
import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.api.model.workspace.AssetSource;
import me.whereareiam.anvil.api.model.workspace.WorkspaceAsset;
import me.whereareiam.anvil.api.model.workspace.WorkspacePlan;
import me.whereareiam.anvil.api.type.Platforms;
import me.whereareiam.anvil.integration.junit.AnvilScenarioFactory;
import me.whereareiam.intercept.testing.fixture.Bundles;
import me.whereareiam.intercept.testing.fixture.Texts;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static me.whereareiam.intercept.testing.environment.InterceptNetwork.A;
import static me.whereareiam.intercept.testing.environment.InterceptNetwork.B;
import static me.whereareiam.intercept.testing.environment.InterceptNetwork.LOBBY_A;
import static me.whereareiam.intercept.testing.environment.InterceptNetwork.LOBBY_B;

/**
 * Builds the network an {@link InterceptNetwork} declaration describes. Paper 26.3 is pinned to build 205, since
 * Anvil does not see newer builds of 26.3 yet.
 */
public final class InterceptNetworkScenarios implements AnvilScenarioFactory<InterceptNetwork> {
	private static final Distribution PAPER = Distribution.remote("26.3", "205");
	private static final Distribution VELOCITY = Distribution.remote("4.3.1", "45");
	private static final Distribution NEOFORGE = Distribution.remote("1.21.1", "21.1.252");

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

	@Override
	public @NotNull AnvilScenario create(@NotNull InterceptNetwork network) {
		AnvilScenario.AnvilScenarioBuilder scenario = AnvilScenario.builder()
				.name("intercept-" + network.platform().name().toLowerCase(Locale.ROOT) + (network.remote() ? "-remote" : ""))
				.entrypoint(A)
				.container(Containers.postgres())
				.container(Containers.redis());
		if (network.remote()) scenario.container(Containers.bundles());

		return switch (network.platform()) {
			case PAPER -> scenario.server(paper(A, network)).server(paper(B, network)).build();
			case VELOCITY -> scenario
					.server(lobby(LOBBY_A))
					.server(lobby(LOBBY_B))
					.proxy(velocity(A, LOBBY_A, network))
					.proxy(velocity(B, LOBBY_B, network))
					.build();
			case NEOFORGE -> scenario.server(neoforge(A, network)).server(neoforge(B, network)).build();
		};
	}

	private static MinecraftServer paper(String name, InterceptNetwork network) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(asset("packetevents-paper", AssetSource.artifact("packetevents-paper"), Path.of("plugins", "packetevents.jar")))
				.asset(asset("intercept-paper", AssetSource.artifact("intercept-paper"), Path.of("plugins", "Intercept.jar")))
				.asset(asset("probe", AssetSource.artifact("probe"), Path.of("plugins", "probe.jar")));

		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.PAPER)
				.distribution(PAPER)
				.workspace(intercept(plan, name, network).build())
				.memoryMegabytes(1536)
				.build();
	}

	private static MinecraftServer lobby(String name) {
		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.PAPER)
				.distribution(PAPER)
				.memoryMegabytes(1024)
				.build();
	}

	private static MinecraftProxy velocity(String name, String lobby, InterceptNetwork network) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(asset("packetevents-velocity", AssetSource.artifact("packetevents-velocity"), Path.of("plugins", "packetevents.jar")))
				.asset(asset("intercept-velocity", AssetSource.artifact("intercept-velocity"), Path.of("plugins", "Intercept.jar")));

		return MinecraftProxy.builder()
				.name(name)
				.platform(Platforms.VELOCITY)
				.distribution(VELOCITY)
				.server(lobby)
				.defaultServer(lobby)
				.workspace(intercept(plan, name, network).build())
				.build();
	}

	private static MinecraftServer neoforge(String name, InterceptNetwork network) {
		WorkspacePlan.WorkspacePlanBuilder plan = WorkspacePlan.builder()
				.asset(asset("intercept-neoforge", AssetSource.artifact("intercept-neoforge"), Path.of("mods", "intercept.jar")));

		return MinecraftServer.builder()
				.name(name)
				.platform(Platforms.NEOFORGE)
				.distribution(NEOFORGE)
				.workspace(intercept(plan, name, network).build())
				.memoryMegabytes(2048)
				.build();
	}

	// Intercept's configuration files and the local texts of Texts, in the platform's folder.
	private static WorkspacePlan.WorkspacePlanBuilder intercept(WorkspacePlan.WorkspacePlanBuilder plan, String instance, InterceptNetwork network) {
		Path folder = network.platform().getFolder();
		String settings = SETTINGS.formatted(instance) + (network.remote() ? Bundles.SETTINGS : "");

		plan.asset(asset("intercept-settings-" + instance + (network.remote() ? "-remote" : ""), settings, folder.resolve("settings.yml")))
				.asset(asset("intercept-persistence", PERSISTENCE, folder.resolve("persistence.yml")))
				.asset(asset("intercept-replication", REPLICATION, folder.resolve("replication.yml")));
		for (Map.Entry<Path, String> text : Texts.local().entrySet()) {
			Path file = folder.resolve("translations").resolve("local").resolve(text.getKey());
			plan.asset(asset("intercept-text-" + text.getKey().toString().replace('/', '-'), text.getValue(), file));
		}

		return plan;
	}

	private static WorkspaceAsset asset(String group, AssetSource source, Path target) {
		return WorkspaceAsset.builder().group(group).source(source).target(target).build();
	}

	private static WorkspaceAsset asset(String group, String text, Path target) {
		return asset(group, AssetSource.text(text), target);
	}
}
