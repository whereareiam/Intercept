package me.whereareiam.intercept.platform.interception.bukkit.common;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.PlatformInteractor;
import me.whereareiam.intercept.type.Version;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public abstract class AbstractPlatformInteractor implements PlatformInteractor {
	@Override
	public Version getServerVersion() {
		return Version.of(Bukkit.getVersion());
	}
}
