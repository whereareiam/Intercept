package me.whereareiam.intercept.platform.interception.bukkit.common.config;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.Locale;

@Singleton
public class BukkitDefaultLocaleProvider implements Provider<Locale> {
	private final Provider<PlatformSettings> settingsProvider;

	@Inject
	public BukkitDefaultLocaleProvider(Provider<PlatformSettings> settingsProvider) {
		this.settingsProvider = settingsProvider;
	}

	@Override
	public Locale get() {
		PlatformSettings settings = settingsProvider.get();
		if (settings != null && settings.getLocale() != null) {
			return settings.getLocale();
		}
		return Locale.ENGLISH;
	}
}
