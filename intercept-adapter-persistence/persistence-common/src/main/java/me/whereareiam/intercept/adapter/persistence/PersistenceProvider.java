package me.whereareiam.intercept.adapter.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code persistence.yml}.
 */
@Singleton
public final class PersistenceProvider implements Provider<Persistence> {
	private final Configura configura;
	private final Path file;
	private Persistence persistence;

	@Inject
	public PersistenceProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("persistence.yml");
	}

	@Override
	public synchronized Persistence get() {
		if (persistence == null) persistence = configura.update(file, Persistence.class);
		return persistence;
	}
}
