package me.whereareiam.intercept.common.config;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.model.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code settings.yml} once, writing the defaults first and adding settings a newer version introduced; a
 * change to it applies after a restart.
 */
@Singleton
public final class SettingsProvider implements Provider<Settings> {
	private final Configura configura;
	private final Path file;
	private Settings settings;

	@Inject
	public SettingsProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("settings.yml");
	}

	@Override
	public synchronized Settings get() {
		if (settings == null) settings = configura.update(file, Settings.class);
		return settings;
	}
}
