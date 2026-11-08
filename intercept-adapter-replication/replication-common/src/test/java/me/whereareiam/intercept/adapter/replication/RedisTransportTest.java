package me.whereareiam.intercept.adapter.replication;

import com.redis.testcontainers.RedisContainer;
import me.whereareiam.intercept.replication.transport.Replication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
class RedisTransportTest {
	@Test
	void aMessageReachesEveryInstanceOnTheChannelAndNoOtherChannel() throws Exception {
		try (RedisContainer redis = new RedisContainer("redis:7.2-alpine")) {
			redis.start();

			BlockingQueue<String> onB = new LinkedBlockingQueue<>();
			BlockingQueue<String> elsewhere = new LinkedBlockingQueue<>();
			try (RedisTransport a = transport(redis, "intercept");
				 RedisTransport b = transport(redis, "intercept");
				 RedisTransport other = transport(redis, "another-network")) {
				b.receive(onB::add);
				other.receive(elsewhere::add);
				// Subscribing happens on the transport's thread; give it a moment before sending.
				Thread.sleep(Duration.ofMillis(500));

				a.send("lobby-1 revision 3");

				assertEquals("lobby-1 revision 3", onB.poll(5, TimeUnit.SECONDS));
				assertNull(elsewhere.poll(1, TimeUnit.SECONDS));
			}
		}
	}

	private static RedisTransport transport(RedisContainer redis, String channel) {
		Replication replication = new Replication();
		replication.setEnabled(true);
		replication.setHost(redis.getHost());
		replication.setPort(redis.getFirstMappedPort());
		replication.setChannel(channel);

		return new RedisTransport(replication, Logger.getAnonymousLogger());
	}
}
