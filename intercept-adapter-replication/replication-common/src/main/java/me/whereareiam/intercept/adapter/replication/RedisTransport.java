package me.whereareiam.intercept.adapter.replication;

import me.whereareiam.intercept.replication.transport.Replication;
import me.whereareiam.intercept.replication.transport.ReplicationTransport;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends and receives through a Redis channel.
 */
public final class RedisTransport implements ReplicationTransport {
	private static final long RECONNECT_MILLIS = 2_000;

	private final JedisPool pool;
	private final String channel;
	private final Logger logger;
	private volatile boolean closed;
	private volatile JedisPubSub subscription;
	private Thread receiver;

	public RedisTransport(@NotNull Replication replication, @NotNull Logger logger) {
		String password = replication.getPassword().isEmpty() ? null : replication.getPassword();
		this.pool = new JedisPool(
				new JedisPoolConfig(),
				replication.getHost(),
				replication.getPort(),
				replication.getTimeout(),
				password,
				replication.isSsl()
		);
		this.channel = replication.getChannel();
		this.logger = logger;
	}

	@Override
	public void send(@NotNull String message) {
		try (Jedis jedis = pool.getResource()) {
			jedis.publish(channel, message);
		}
	}

	@Override
	public synchronized void receive(@NotNull Consumer<String> receiver) {
		if (this.receiver != null) throw new IllegalStateException("The transport is already receiving");

		this.receiver = Thread.ofPlatform()
				.daemon()
				.name("Intercept replication")
				.start(() -> listen(receiver));
	}

	@Override
	public void close() {
		closed = true;

		JedisPubSub current = subscription;
		if (current != null && current.isSubscribed()) current.unsubscribe();
		if (receiver != null) receiver.interrupt();
		pool.close();
	}

	private void listen(Consumer<String> receiver) {
		while (!closed) {
			try (Jedis jedis = pool.getResource()) {
				JedisPubSub pubSub = new JedisPubSub() {
					@Override
					public void onMessage(String channel, String message) {
						try {
							receiver.accept(message);
						} catch (RuntimeException failure) {
							logger.log(Level.WARNING, "Cannot handle a replicated message", failure);
						}
					}
				};
				subscription = pubSub;
				jedis.subscribe(pubSub, channel);
			} catch (RuntimeException failure) {
				if (closed) return;
				logger.warning("Lost the connection to Redis, reconnecting: " + failure.getMessage());
			}

			if (!pause()) return;
		}
	}

	private boolean pause() {
		try {
			Thread.sleep(RECONNECT_MILLIS);
			return true;
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
