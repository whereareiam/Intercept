package me.whereareiam.intercept.platform.neoforge.command;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.command.CommandSource;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;

/**
 * Builds Cloud's NeoForge command manager, which registers Intercept's commands when the server builds its own.
 */
@Singleton
public final class NeoForgeCommandManagerProvider implements Provider<CommandManager<CommandSource>> {
	private CommandManager<CommandSource> manager;

	@Override
	public synchronized CommandManager<CommandSource> get() {
		if (manager == null) {
			manager = new NeoForgeServerCommandManager<>(
					ExecutionCoordinator.simpleCoordinator(),
					SenderMapper.create(NeoForgeCommandSource::new, source -> ((NeoForgeCommandSource) source).stack())
			);
		}

		return manager;
	}
}
