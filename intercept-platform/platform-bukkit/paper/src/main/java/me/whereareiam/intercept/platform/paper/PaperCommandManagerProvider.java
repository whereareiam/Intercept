package me.whereareiam.intercept.platform.paper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.platform.paper.player.PaperInterceptConsole;
import me.whereareiam.intercept.platform.paper.player.PaperInterceptPlayer;
import me.whereareiam.keystone.model.Actor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * Provides CommandManager<Actor> for Paper platform.
 * Creates and configures the appropriate CommandManager with Bukkit CommandSender -> Actor mapping.
 * <p>
 * Maps to PaperInterceptPlayer for players and PaperInterceptConsole for console.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PaperCommandManagerProvider implements Provider<CommandManager<Actor>> {
	private final Plugin plugin;
	private CommandManager<Actor> commandManager;

	@Override
	@NotNull
	public CommandManager<Actor> get() {
		if (commandManager != null) return commandManager;

		SenderMapper<CommandSender, Actor> senderMapper = SenderMapper.create(
				this::map,
				this::map
		);

		commandManager = new LegacyPaperCommandManager<>(
				plugin,
				ExecutionCoordinator.asyncCoordinator(),
				senderMapper
		);

		return commandManager;
	}

	private Actor map(CommandSender sender) {
		if (sender instanceof ConsoleCommandSender consoleSender) return new PaperInterceptConsole(consoleSender);
		if (sender instanceof Player player) return new PaperInterceptPlayer(player);

		throw new UnsupportedOperationException("Unsupported command sender type: " + sender.getClass().getName());
	}

	private CommandSender map(Actor actor) {
		if (actor instanceof PaperInterceptPlayer paperPlayer) return paperPlayer.getBukkitPlayer();
		if (actor instanceof PaperInterceptConsole paperConsole) return paperConsole.getConsoleSender();

		throw new UnsupportedOperationException("Cannot reverse map Actor to Bukkit CommandSender");
	}
}