package me.whereareiam.intercept.platform.paper.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.command.CommandSource;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * Builds Cloud's Paper command manager, registered through Paper's Brigadier commands.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PaperCommandManagerProvider implements Provider<CommandManager<CommandSource>> {
	private final @NotNull Plugin plugin;
	private CommandManager<CommandSource> manager;

	@Override
	public synchronized CommandManager<CommandSource> get() {
		if (manager == null) {
			manager = PaperCommandManager.builder(SenderMapper.<CommandSourceStack, CommandSource>create(
							PaperCommandSource::new,
							source -> ((PaperCommandSource) source).stack()
					))
					.executionCoordinator(ExecutionCoordinator.simpleCoordinator())
					.buildOnEnable(plugin);
		}

		return manager;
	}
}
