package me.whereareiam.intercept.platform.paper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Reads a player's chosen language before they join, so the client settings they send next are already rewritten,
 * and forgets it when they leave.
 */
@Singleton
public final class PaperPlayers implements Listener, Lifecycle {
	private final Plugin plugin;
	private final LanguageService languages;

	@Inject
	public PaperPlayers(@NotNull Plugin plugin, @NotNull LanguageService languages) {
		this.plugin = plugin;
		this.languages = languages;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void stop() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
		languages.load(event.getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		languages.unload(event.getPlayer().getUniqueId());
	}
}
