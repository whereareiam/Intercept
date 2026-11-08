package me.whereareiam.intercept.common.replication;

import me.whereareiam.intercept.model.replication.Replicated;
import me.whereareiam.intercept.replication.ReplicationService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Replication for an instance that runs alone: nothing is sent and nothing arrives.
 */
public final class LocalReplicationService implements ReplicationService {
	@Override
	public void publish(@NotNull Replicated message) {
	}

	@Override
	public <T extends Replicated> void subscribe(@NotNull Class<T> type, @NotNull Consumer<T> listener) {
	}
}
