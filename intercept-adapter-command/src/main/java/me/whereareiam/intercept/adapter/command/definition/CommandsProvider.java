package me.whereareiam.intercept.adapter.command.definition;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code commands.yml} in the configuration phase, writing the defaults first and adding settings a newer
 * version introduced. Reading it earlier would be a mistake in Intercept's startup order, so {@link #get()}
 * fails then.
 */
@Singleton
public final class CommandsProvider implements Provider<Commands>, RuntimeLifecycle {
	private final Configura configura;
	private final Path file;
	private volatile Commands commands;

	@Inject
	public CommandsProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("commands.yml");
	}

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONFIGURATION;
	}

	@Override
	public void initialize() {
		commands = configura.update(file, Commands.class);
	}

	/**
	 * Keeps the file's values: what stops after this lifecycle may still read them.
	 */
	@Override
	public void shutdown() {
	}

	@Override
	public @NotNull Commands get() {
		Commands current = commands;
		if (current == null)
			throw new IllegalStateException(file.getFileName() + " is read in the configuration phase, which has not run yet");

		return current;
	}
}
