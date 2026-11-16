package me.whereareiam.intercept.platform.velocity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Reads a player's chosen language while they log in and sets it as their effective locale once they are in.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class VelocityPlayers implements RuntimeLifecycle {
	private final @NotNull ProxyServer proxy;
	private final @Named("plugin") @NotNull Object plugin;
	private final @NotNull LanguageService languages;

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONNECTIONS;
	}

	@Override
	public void initialize() {
		proxy.getEventManager().register(plugin, this);
	}

	@Override
	public void shutdown() {
		proxy.getEventManager().unregisterListener(plugin, this);
	}

	@Subscribe(order = PostOrder.LAST)
	public void onLogin(LoginEvent event) {
		if (event.getResult().isAllowed()) languages.load(event.getPlayer().getUniqueId());
	}

	@Subscribe
	public void onPostLogin(PostLoginEvent event) {
		languages.chosen(event.getPlayer().getUniqueId()).ifPresent(event.getPlayer()::setEffectiveLocale);
	}

	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		languages.unload(event.getPlayer().getUniqueId());
	}
}
