package me.whereareiam.intercept.platform.direct.oraylen.listener.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.registry.PlayerRegistry;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PlayerDisconnectListener implements DynamicListener<PlayerDisconnectEvent> {
	private final PlayerRegistry playerRegistry;

	@Override
	public void onEvent(PlayerDisconnectEvent event) {
		Player player = event.getPlayer();
		playerRegistry.removePlayerData(player.getUuid());
	}
}
