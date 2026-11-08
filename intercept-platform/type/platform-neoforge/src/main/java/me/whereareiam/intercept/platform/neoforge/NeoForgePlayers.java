package me.whereareiam.intercept.platform.neoforge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Reads a player's chosen language as they log in and forgets it when they leave.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class NeoForgePlayers implements Lifecycle {
	private final @NotNull LanguageService languages;

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::loggedIn);
		NeoForge.EVENT_BUS.addListener(this::loggedOut);
	}

	// NeoForge cannot remove a listener added as a method reference; Intercept stops only when the server does.
	@Override
	public void stop() {
	}

	private void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		languages.load(event.getEntity().getUUID());
	}

	private void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		languages.unload(event.getEntity().getUUID());
	}
}
