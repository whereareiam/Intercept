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
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.jetbrains.annotations.NotNull;

/**
 * Reads a player's chosen language while they log in and sets it as their effective locale once they are in.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class VelocityPlayers implements Lifecycle {
	private final @NotNull ProxyServer proxy;
	private final @Named("plugin") @NotNull Object plugin;
	private final @NotNull LanguageService languages;

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		proxy.getEventManager().register(plugin, this);
	}

	@Override
	public void stop() {
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
