package me.whereareiam.intercept.platform.paper;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.interception.library.packetevents.ClientLocales;
import me.whereareiam.intercept.interception.library.packetevents.PacketEventsInterception;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.library.LibraryLoader;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.platform.PlatformConfiguration;
import me.whereareiam.intercept.platform.library.AttacheLibraryLoader;
import me.whereareiam.intercept.platform.paper.command.PaperCommandManagerProvider;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * Paper's part: PacketEvents translates system messages and reports each player's chosen language to the server in
 * place of their client's, so Paper renders keys in it.
 */
public final class PaperConfiguration extends PlatformConfiguration {
	private final Plugin plugin;
	private final LibraryManager libraries;

	public PaperConfiguration(@NotNull Plugin plugin, @NotNull LibraryManager libraries) {
		super(plugin.getDataFolder().toPath(), plugin.getLogger());
		this.plugin = plugin;
		this.libraries = libraries;
	}

	@Override
	protected void configurePlatform() {
		bind(Plugin.class).toInstance(plugin);
		bind(LibraryManager.class).toInstance(libraries);
		bindConstant().annotatedWith(Names.named("rewriteClientLocale")).to(true);

		Multibinder<Lifecycle> lifecycles = Multibinder.newSetBinder(binder(), Lifecycle.class);
		lifecycles.addBinding().to(PacketEventsInterception.class);
		lifecycles.addBinding().to(PaperPlayers.class);
	}

	@Override
	protected @NotNull Class<? extends LibraryLoader> libraryLoader() {
		return AttacheLibraryLoader.class;
	}

	@Override
	protected @NotNull Class<? extends LanguageApplier> languageApplier() {
		return ClientLocales.class;
	}

	@Override
	protected @NotNull Class<? extends com.google.inject.Provider<CommandManager<CommandSource>>> commandManager() {
		return PaperCommandManagerProvider.class;
	}
}
