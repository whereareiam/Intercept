package me.whereareiam.intercept.platform.paper.listener.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.registry.PlayerRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PlayerQuitListener implements DynamicListener<PlayerQuitEvent> {
	private final PlayerRegistry playerRegistry;

	public void onEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		playerRegistry.removePlayerData(player.getUniqueId());
	}
}
