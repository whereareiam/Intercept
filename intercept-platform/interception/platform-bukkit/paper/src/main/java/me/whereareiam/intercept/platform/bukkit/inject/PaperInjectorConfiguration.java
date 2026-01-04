package me.whereareiam.intercept.platform.bukkit.inject;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.PlatformInteractor;
import me.whereareiam.intercept.Scheduler;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.platform.bukkit.PaperPlatformInteractor;
import me.whereareiam.intercept.platform.bukkit.PaperScheduler;
import me.whereareiam.intercept.platform.bukkit.command.PaperCommandManagerProvider;
import me.whereareiam.intercept.platform.bukkit.listener.PaperListenerRegistrar;
import me.whereareiam.keystone.Actor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.incendo.cloud.CommandManager;

@RequiredArgsConstructor
public class PaperInjectorConfiguration extends AbstractModule {
	private final Plugin plugin;
	private final LibraryManager libraryManager;

	@Override
	protected void configure() {
		bind(Plugin.class).toInstance(plugin);
		bind(PluginManager.class).toInstance(plugin.getServer().getPluginManager());

		bind(Scheduler.class).to(PaperScheduler.class);
		bind(ListenerRegistrar.class).to(PaperListenerRegistrar.class);
		bind(PlatformInteractor.class).to(PaperPlatformInteractor.class);
		bind(new TypeLiteral<CommandManager<Actor>>() {}).toProvider(PaperCommandManagerProvider.class);
		bind(LibraryManager.class).toInstance(libraryManager);
	}
}