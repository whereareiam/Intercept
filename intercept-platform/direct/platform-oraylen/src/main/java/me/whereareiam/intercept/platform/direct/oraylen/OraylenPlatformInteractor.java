package me.whereareiam.intercept.platform.direct.oraylen;

import com.google.inject.Singleton;
import me.whereareiam.intercept.PlatformInteractor;
import me.whereareiam.intercept.type.Version;
import net.minestom.server.MinecraftServer;

@Singleton
public final class OraylenPlatformInteractor implements PlatformInteractor {
	@Override
	public Version getServerVersion() {
		return Version.of(MinecraftServer.VERSION_NAME);
	}
}
