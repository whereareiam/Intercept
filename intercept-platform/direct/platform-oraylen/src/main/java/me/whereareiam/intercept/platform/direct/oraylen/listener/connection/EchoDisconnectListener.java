package me.whereareiam.intercept.platform.direct.oraylen.listener.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.listener.DynamicListener;
import me.whereareiam.intercept.registry.PlayerRegistry;
import net.oraylen.api.event.type.player.EchoDisconnectEvent;
import net.oraylen.api.model.actor.Echo;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class EchoDisconnectListener implements DynamicListener<EchoDisconnectEvent> {
	private final PlayerRegistry playerRegistry;

	@Override
	public void onEvent(EchoDisconnectEvent event) {
		Echo echo = event.getEcho();
		if (echo == null) return;
		playerRegistry.removePlayerData(echo.getUniqueId());
	}
}
