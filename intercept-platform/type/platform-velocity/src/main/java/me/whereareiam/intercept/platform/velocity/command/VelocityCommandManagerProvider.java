package me.whereareiam.intercept.platform.velocity.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import me.whereareiam.intercept.command.CommandSource;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * Builds Cloud's Velocity command manager.
 */
@Singleton
public final class VelocityCommandManagerProvider implements Provider<CommandManager<CommandSource>> {
	private final PluginContainer container;
	private final ProxyServer proxy;
	private CommandManager<CommandSource> manager;

	@Inject
	public VelocityCommandManagerProvider(@NotNull PluginContainer container, @NotNull ProxyServer proxy) {
		this.container = container;
		this.proxy = proxy;
	}

	@Override
	public synchronized CommandManager<CommandSource> get() {
		if (manager == null) {
			manager = new VelocityCommandManager<>(
					container,
					proxy,
					ExecutionCoordinator.simpleCoordinator(),
					SenderMapper.create(VelocityCommandSource::new, source -> ((VelocityCommandSource) source).source())
			);
		}

		return manager;
	}
}
