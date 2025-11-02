package me.whereareiam.intercept.platform.paper.inject;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.platform.paper.listener.PaperListenerRegistrar;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

@RequiredArgsConstructor
public class PaperInjectorConfiguration extends AbstractModule {
	private final Plugin plugin;

	@Override
	protected void configure() {
		bind(Plugin.class).toInstance(plugin);
		bind(PluginManager.class).toInstance(plugin.getServer().getPluginManager());

		bind(ListenerRegistrar.class).to(PaperListenerRegistrar.class);
	}
}
