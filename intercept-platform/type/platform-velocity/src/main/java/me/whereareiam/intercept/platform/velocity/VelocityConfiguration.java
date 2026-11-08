package me.whereareiam.intercept.platform.velocity;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.interception.library.packetevents.PacketEventsInterception;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.library.LibraryLoader;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.platform.PlatformConfiguration;
import me.whereareiam.intercept.platform.library.AttacheLibraryLoader;
import me.whereareiam.intercept.platform.velocity.command.VelocityCommandManagerProvider;
import me.whereareiam.intercept.platform.logging.Slf4jHandler;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Velocity's part: the effective locale carries a player's chosen language, so the proxy renders keys in it; with
 * PacketEvents on the proxy, system messages passing through it are translated as well.
 */
public final class VelocityConfiguration extends PlatformConfiguration {
	private final Object plugin;
	private final ProxyServer proxy;
	private final PluginContainer container;
	private final LibraryManager libraries;

	public VelocityConfiguration(
			@NotNull Object plugin,
			@NotNull ProxyServer proxy,
			@NotNull PluginContainer container,
			@NotNull Logger logger,
			@NotNull Path dataPath,
			@NotNull LibraryManager libraries
	) {
		super(dataPath, Slf4jHandler.logger(logger));
		this.plugin = plugin;
		this.proxy = proxy;
		this.container = container;
		this.libraries = libraries;
	}

	@Override
	protected void configurePlatform() {
		bind(Object.class).annotatedWith(Names.named("plugin")).toInstance(plugin);
		bind(ProxyServer.class).toInstance(proxy);
		bind(PluginContainer.class).toInstance(container);
		bind(LibraryManager.class).toInstance(libraries);
		bindConstant().annotatedWith(Names.named("rewriteClientLocale")).to(false);

		Multibinder<Lifecycle> lifecycles = Multibinder.newSetBinder(binder(), Lifecycle.class);
		lifecycles.addBinding().to(VelocityPlayers.class);
		if (proxy.getPluginManager().isLoaded("packetevents"))
			lifecycles.addBinding().to(PacketEventsInterception.class);
	}

	@Override
	protected @NotNull Class<? extends LibraryLoader> libraryLoader() {
		return AttacheLibraryLoader.class;
	}

	@Override
	protected @NotNull Class<? extends LanguageApplier> languageApplier() {
		return EffectiveLocaleApplier.class;
	}

	@Override
	protected @NotNull Class<? extends com.google.inject.Provider<CommandManager<CommandSource>>> commandManager() {
		return VelocityCommandManagerProvider.class;
	}
}
