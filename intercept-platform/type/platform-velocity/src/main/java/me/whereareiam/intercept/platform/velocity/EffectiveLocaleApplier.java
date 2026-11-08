package me.whereareiam.intercept.platform.velocity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.proxy.ProxyServer;
import me.whereareiam.intercept.language.LanguageApplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Sets a player's effective locale, which Velocity renders keys in; {@code null} goes back to the client's.
 */
@Singleton
public final class EffectiveLocaleApplier implements LanguageApplier {
	private final ProxyServer proxy;

	@Inject
	public EffectiveLocaleApplier(@NotNull ProxyServer proxy) {
		this.proxy = proxy;
	}

	@Override
	public void apply(@NotNull UUID uniqueId, @Nullable Locale language) {
		proxy.getPlayer(uniqueId).ifPresent(player -> player.setEffectiveLocale(language));
	}
}
