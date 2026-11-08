package me.whereareiam.intercept.adapter.replication;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.library.LibraryLoader;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.replication.Replicated;
import me.whereareiam.intercept.replication.ReplicationService;
import me.whereareiam.intercept.replication.transport.Replication;
import me.whereareiam.intercept.replication.transport.ReplicationTransport;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Replicates through a transport when {@code replication.yml} enables it, and keeps everything on this instance
 * otherwise.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class TransportReplicationService implements ReplicationService, Lifecycle {
	private final @NotNull Provider<Replication> replication;
	private final @NotNull Provider<Settings> settings;
	private final @NotNull LibraryLoader libraries;
	private final @Named("intercept") @NotNull Logger logger;
	private final Map<Class<?>, List<Consumer<Replicated>>> listeners = new ConcurrentHashMap<>();

	private String instance;
	private ReplicationTransport transport;

	@Override
	public @NotNull Phase phase() {
		return Phase.REPLICATION;
	}

	@Override
	public void start() {
		String configured = settings.get().getInstance();
		instance = configured.isBlank() ? UUID.randomUUID().toString() : configured.replace(' ', '-');

		Replication replication = this.replication.get();
		if (!replication.isEnabled()) return;

		libraries.load("redis");
		transport = new RedisTransport(replication, logger);
		transport.receive(this::receive);
		logger.info("Replicating through Redis at " + replication.getHost() + ":" + replication.getPort()
				+ ", channel " + replication.getChannel() + ", as " + instance);
	}

	@Override
	public void stop() {
		if (transport != null) transport.close();
		transport = null;
	}

	@Override
	public void publish(@NotNull Replicated message) {
		ReplicationTransport current = transport;
		if (current == null) return;

		try {
			current.send(MessageCodec.encode(instance, message));
		} catch (RuntimeException failure) {
			logger.log(Level.WARNING, "Cannot replicate " + message, failure);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Replicated> void subscribe(@NotNull Class<T> type, @NotNull Consumer<T> listener) {
		listeners.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).add((Consumer<Replicated>) listener);
	}

	private void receive(String line) {
		MessageCodec.Decoded decoded = MessageCodec.decode(line);
		if (decoded == null || decoded.instance().equals(instance)) return;

		listeners.getOrDefault(decoded.message().getClass(), List.of())
				.forEach(listener -> listener.accept(decoded.message()));
	}
}
