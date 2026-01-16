package me.whereareiam.intercept.platform.direct.oraylen.listener.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.platform.direct.oraylen.actor.player.OraylenInterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import net.oraylen.api.event.type.player.EchoSpawnEvent;
import net.oraylen.api.model.actor.Echo;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class EchoSpawnListener implements DynamicListener<EchoSpawnEvent> {
	private final PlayerRegistry playerRegistry;

	@Override
	public void onEvent(EchoSpawnEvent event) {
		if (!event.isFirstSpawn()) return;

		Echo echo = event.getEcho();
		if (echo == null) return;
		playerRegistry.syncPlayerData(new OraylenInterceptPlayer(echo));
	}
}
