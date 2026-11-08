package me.whereareiam.intercept.platform.velocity;

import com.google.inject.Injector;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.common.Intercept;
import me.whereareiam.intercept.platform.InterceptInjector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Intercept while the proxy runs: built once its libraries are loaded.
 */
final class VelocityRuntime {
	private final Logger logger;
	private final Intercept intercept;

	VelocityRuntime(
			@NotNull Object plugin,
			@NotNull ProxyServer proxy,
			@NotNull PluginContainer container,
			@NotNull Logger logger,
			@NotNull Path dataPath,
			@NotNull LibraryManager libraries
	) {
		this.logger = logger;

		VelocityConfiguration platform = new VelocityConfiguration(plugin, proxy, container, logger, dataPath, libraries);
		Injector injector = InterceptInjector.create(platform);
		this.intercept = injector.getInstance(Intercept.class);
	}

	void start() {
		try {
			intercept.start();
		} catch (RuntimeException failure) {
			logger.error("Intercept cannot start", failure);
		}
	}

	void stop() {
		intercept.stop();
	}
}
