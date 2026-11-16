package me.whereareiam.intercept.adapter.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code persistence.yml} in the configuration phase, writing the defaults first and adding settings a newer
 * version introduced. Reading it earlier would be a mistake in Intercept's startup order, so {@link #get()}
 * fails then.
 */
@Singleton
public final class PersistenceProvider implements Provider<Persistence>, RuntimeLifecycle {
	private final Configura configura;
	private final Path file;
	private volatile Persistence persistence;

	@Inject
	public PersistenceProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("persistence.yml");
	}

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONFIGURATION;
	}

	@Override
	public void initialize() {
		persistence = configura.update(file, Persistence.class);
	}

	/**
	 * Keeps the file's values: what stops after this lifecycle may still read them.
	 */
	@Override
	public void shutdown() {
	}

	@Override
	public @NotNull Persistence get() {
		Persistence current = persistence;
		if (current == null)
			throw new IllegalStateException(file.getFileName() + " is read in the configuration phase, which has not run yet");

		return current;
	}
}
