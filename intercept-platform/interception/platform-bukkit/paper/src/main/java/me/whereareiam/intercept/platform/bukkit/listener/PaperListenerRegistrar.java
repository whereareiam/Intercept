package me.whereareiam.intercept.platform.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.interception.bukkit.common.CommonListenerRegistrar;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.PlatformSettings;
import me.whereareiam.intercept.platform.interception.bukkit.common.util.BukkitUtil;
import me.whereareiam.intercept.platform.bukkit.listener.connection.PlayerQuitListener;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

@Singleton
public class PaperListenerRegistrar extends CommonListenerRegistrar {
	private final Injector injector;
	private final Plugin plugin;
	private final PluginManager pluginManager;

	@Inject
	public PaperListenerRegistrar(
			Injector injector,
			Provider<PlatformSettings> platformSettingsProvider,
			Plugin plugin,
			PluginManager pluginManager
	) {
		super(platformSettingsProvider);
		this.injector = injector;
		this.plugin = plugin;
		this.pluginManager = pluginManager;
	}

	@Override
	public void registerListeners() {
		registerListener(PlayerQuitEvent.class, injector.getInstance(PlayerQuitListener.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void registerListener(Class<T> eventClass, DynamicListener<T> listener) {
		PlatformSettings.Listeners listeners = settings.get().getListeners();
		if (listeners == null || listeners.getEvents() == null
				|| listeners.getEvents().get(eventClass.getName()) == null
				|| !listeners.getEvents().get(eventClass.getName()).isRegister()) return;
		Logger.debug("Registering listener for event " + eventClass.getName());

		pluginManager.registerEvent(
				(Class<? extends Event>) eventClass,
				new Listener() {
				},
				BukkitUtil.of(determinePriority(eventClass)),
				(l, e) -> listener.onEvent(eventClass.cast(e)),
				plugin
		);
	}
}
