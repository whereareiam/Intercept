package me.whereareiam.intercept.platform.neoforge;

import com.google.inject.Injector;
import com.mojang.logging.LogUtils;
import me.whereareiam.intercept.common.Intercept;
import me.whereareiam.intercept.platform.InterceptInjector;
import me.whereareiam.intercept.platform.logging.Slf4jHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Intercept on NeoForge. It starts when the dedicated server is set up, before the server builds its commands, so
 * Intercept's commands are there from the start, and stops when the server has stopped.
 */
@Mod("intercept")
public final class NeoForgeIntercept {
	private final Logger logger = LogUtils.getLogger();
	private final Path folder = FMLPaths.GAMEDIR.get().resolve("intercept");
	private Intercept intercept;

	public NeoForgeIntercept(IEventBus modBus) {
		modBus.addListener(this::setUp);
		NeoForge.EVENT_BUS.addListener(this::stopped);
	}

	private void setUp(FMLDedicatedServerSetupEvent event) {
		try {
			Injector injector = InterceptInjector.create(new NeoForgeConfiguration(folder, Slf4jHandler.logger(logger)));
			intercept = injector.getInstance(Intercept.class);
			intercept.start();
		} catch (RuntimeException failure) {
			logger.error("Intercept did not start; the server runs without it", failure);
			intercept = null;
		}
	}

	private void stopped(ServerStoppedEvent event) {
		if (intercept != null) intercept.stop();
		intercept = null;
	}
}
