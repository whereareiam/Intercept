package me.whereareiam.intercept.platform.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import jakarta.inject.Inject;
import me.whereareiam.attache.platform.velocity.VelocityLibraryManager;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Intercept on Velocity. Libraries load first, so this class only names Attache and Velocity's own types; Intercept
 * itself starts in {@link VelocityRuntime}. Velocity's own Guice builds this class, so it is injected through
 * {@code jakarta.inject}, which Intercept's relocated Guice leaves alone. {@code velocity-plugin.json} declares it.
 */
public final class VelocityIntercept {
	private final ProxyServer proxy;
	private final PluginContainer container;
	private final Logger logger;
	private final Path dataPath;
	private VelocityRuntime runtime;

	@Inject
	public VelocityIntercept(ProxyServer proxy, PluginContainer container, Logger logger, @DataDirectory Path dataPath) {
		this.proxy = proxy;
		this.container = container;
		this.logger = logger;
		this.dataPath = dataPath;
	}

	@Subscribe
	public void onInitialize(ProxyInitializeEvent event) {
		VelocityLibraryManager libraries = new VelocityLibraryManager(proxy, container, logger, dataPath, ".libraries");
		libraries.loadDescriptors();

		runtime = new VelocityRuntime(this, proxy, container, logger, dataPath, libraries);
		runtime.start();
	}

	@Subscribe
	public void onShutdown(ProxyShutdownEvent event) {
		if (runtime != null) runtime.stop();
		runtime = null;
	}
}
