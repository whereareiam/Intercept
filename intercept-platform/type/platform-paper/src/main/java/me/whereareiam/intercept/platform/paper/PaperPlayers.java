package me.whereareiam.intercept.platform.paper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
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
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PaperPlayers implements Listener, RuntimeLifecycle {
	private final @NotNull Plugin plugin;
	private final @NotNull LanguageService languages;

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONNECTIONS;
	}

	@Override
	public void initialize() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void shutdown() {
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
