package me.whereareiam.intercept.common.config;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import me.whereareiam.intercept.model.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code settings.yml} in the configuration phase, writing the defaults first and adding settings a newer
 * version introduced; a change to it applies after a restart. Reading it earlier would be a mistake in Intercept's
 * startup order, so {@link #get()} fails then.
 */
@Singleton
public final class SettingsProvider implements Provider<Settings>, RuntimeLifecycle {
	private final Configura configura;
	private final Path file;
	private volatile Settings settings;

	@Inject
	public SettingsProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("settings.yml");
	}

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONFIGURATION;
	}

	@Override
	public void initialize() {
		settings = configura.update(file, Settings.class);
	}

	/**
	 * Keeps the file's values: what stops after this lifecycle may still read them.
	 */
	@Override
	public void shutdown() {
	}

	@Override
	public @NotNull Settings get() {
		Settings current = settings;
		if (current == null)
			throw new IllegalStateException(file.getFileName() + " is read in the configuration phase, which has not run yet");

		return current;
	}
}
