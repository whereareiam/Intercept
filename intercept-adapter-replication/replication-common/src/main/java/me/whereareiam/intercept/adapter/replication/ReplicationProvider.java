package me.whereareiam.intercept.adapter.replication;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.replication.transport.Replication;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Reads {@code replication.yml}.
 */
@Singleton
public final class ReplicationProvider implements Provider<Replication> {
	private final Configura configura;
	private final Path file;
	private Replication replication;

	@Inject
	public ReplicationProvider(@NotNull Configura configura, @Named("dataPath") @NotNull Path dataPath) {
		this.configura = configura;
		this.file = dataPath.resolve("replication.yml");
	}

	@Override
	public synchronized Replication get() {
		if (replication == null) replication = configura.update(file, Replication.class);
		return replication;
	}
}
