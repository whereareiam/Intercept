package me.whereareiam.intercept.platform.paper;

import me.whereareiam.intercept.DependencyResolver;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.lifecycle.InterceptBootstrappedEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.platform.common.BukkitLoggingHelper;
import me.whereareiam.intercept.platform.paper.inject.PaperInjector;
import me.whereareiam.intercept.type.PluginType;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

public class PaperIntercept extends JavaPlugin {
	private final Path dataPath = getDataFolder().toPath();
	private final Logger logger = getLogger();
	private PaperInjector paperInjector;

	@Override
	public void onLoad() {
		PluginType.setPluginType(PluginType.PAPER);
		BukkitLoggingHelper.setLogger(logger);

		// Load dependencies first
		DependencyResolver dependencyResolver = new PaperDependencyResolver(this);
		dependencyResolver.loadLibraries();
		dependencyResolver.resolveDependencies();

		paperInjector = new PaperInjector(this, dataPath);

		// Call InterceptBootstrappedEvent after core infrastructure is initialized
		EventManager eventManager = paperInjector.getInjector().getInstance(EventManager.class);
		eventManager.call(new InterceptBootstrappedEvent());
	}

	@Override
	public void onEnable() {
		// Call InterceptReadyEvent after plugin is fully operational
		EventManager eventManager = paperInjector.getInjector().getInstance(EventManager.class);
		eventManager.call(new InterceptReadyEvent());
	}
}