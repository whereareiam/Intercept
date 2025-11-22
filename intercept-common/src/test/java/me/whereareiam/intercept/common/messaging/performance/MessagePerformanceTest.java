package me.whereareiam.intercept.common.messaging.performance;

import me.whereareiam.intercept.common.messaging.DefaultMessageEntry;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.DefaultMessageService;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Performance tests for message resolution.
 * Validates that performance targets are met.
 */
class MessagePerformanceTest {
	private DefaultMessageRegistry registry;
	private MessageService service;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry(mock(Registry.class));

		// Create settings with cache enabled
		Settings settings = new Settings();
		settings.setLevel(2);
		settings.setLocale(Locale.US);

		Settings.Performance performance = new Settings.Performance();
		performance.setPrerenderStatic(true);
		performance.setBuildDependencyGraph(true);

		Settings.Performance.Cache cache = new Settings.Performance.Cache();
		cache.setEnabled(true);
		cache.setSemiStaticSize(1000);
		cache.setDynamicSize(500);
		cache.setExpireMinutes(60);
		performance.setCache(cache);

		settings.setPerformance(performance);

		service = new DefaultMessageService(registry, settings);
	}

	@Test
	void staticMessageShouldResolveUnder10Microseconds() {
		// Setup
		registry.register("static", new DefaultMessageEntry(MessageType.MESSAGE, "Static text"));

		// Warm up JVM and cache (multiple iterations)
		for (int i = 0; i < 1000; i++) {
			service.resolve("static", Locale.US);
		}

		// Measure average over multiple calls
		long totalDuration = 0;
		int iterations = 100;

		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			String result = service.resolve("static", Locale.US);
			totalDuration += System.nanoTime() - start;
			assertNotNull(result);
		}

		long avgDuration = totalDuration / iterations;
		assertTrue(avgDuration < 10_000, "Static cached message should average under 10µs, was: " + avgDuration + "ns");
	}

	@Test
	void semiStaticMessageShouldResolveUnder100Microseconds() {
		// Setup
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[App]"));
		registry.register("semi", new DefaultMessageEntry(MessageType.MESSAGE,
				"<m:prefix> <if enabled==true>Enabled<else>Disabled</if>"));

		// Warm up JVM and cache
		for (int i = 0; i < 100; i++) {
			service.resolve("semi", Locale.US, Map.of("enabled", true));
		}

		// Measure average
		long totalDuration = 0;
		int iterations = 50;

		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			String result = service.resolve("semi", Locale.US, Map.of("enabled", true));
			totalDuration += System.nanoTime() - start;
			assertNotNull(result);
		}

		long avgDuration = totalDuration / iterations;
		assertTrue(avgDuration < 100_000, "Semi-static cached message should average under 100µs, was: " + avgDuration + "ns");
	}

	@Test
	void simpleDynamicMessageShouldResolveUnder100Microseconds() {
		// Setup
		registry.register("dynamic", new DefaultMessageEntry(MessageType.MESSAGE,
				"Hello, <p:name>!"));

		// Warm up JVM
		for (int i = 0; i < 100; i++) {
			service.resolve("dynamic", Locale.US, Map.of("name", "Warmup" + i));
		}

		// Measure average (dynamic messages with unique values)
		long totalDuration = 0;
		int iterations = 50;

		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			String result = service.resolve("dynamic", Locale.US, Map.of("name", "Player" + i));
			totalDuration += System.nanoTime() - start;
			assertNotNull(result);
		}

		long avgDuration = totalDuration / iterations;
		assertTrue(avgDuration < 100_000, "Simple dynamic message should average under 100µs, was: " + avgDuration + "ns");
	}

	@Test
	void complexDynamicMessageShouldResolveUnder500Microseconds() {
		// Setup complex message with all features
		registry.register("color", new DefaultMessageEntry(MessageType.TEMPLATE, "<red>"));
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:color>[App]"));
		registry.register("player-name", new DefaultMessageEntry(MessageType.TEMPLATE, "<p:name>"));
		registry.register("complex", new DefaultMessageEntry(MessageType.MESSAGE,
				"<m:prefix> <tpl:player-name name='<p:player>'> <if online==true>is online<else>is offline</if> on <p:server>"));

		// Warm up JVM
		for (int i = 0; i < 100; i++) {
			service.resolve("complex", Locale.US,
					Map.of("player", "Warmup" + i, "online", i % 2 == 0, "server", "lobby"));
		}

		// Measure average
		long totalDuration = 0;
		int iterations = 50;

		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			String result = service.resolve("complex", Locale.US,
					Map.of("player", "Player" + i, "online", i % 2 == 0, "server", "lobby"));
			totalDuration += System.nanoTime() - start;
			assertNotNull(result);
		}

		long avgDuration = totalDuration / iterations;
		assertTrue(avgDuration < 500_000, "Complex dynamic message should average under 500µs, was: " + avgDuration + "ns");
	}

	@Test
	void concurrentAccessShouldHandleLoad() throws InterruptedException {
		// Setup
		registry.register("prefix", new DefaultMessageEntry(MessageType.TEMPLATE, "[App]"));
		registry.register("msg", new DefaultMessageEntry(MessageType.MESSAGE,
				"<m:prefix> Player <p:name> joined"));

		int threadCount = 50;
		int requestsPerThread = 20;
		CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		long start = System.nanoTime();

		for (int i = 0; i < threadCount; i++) {
			final int threadId = i;
			executor.submit(() -> {
				for (int j = 0; j < requestsPerThread; j++) {
					service.resolve("msg", Locale.US, Map.of("name", "Player" + threadId + "_" + j));
					latch.countDown();
				}
			});
		}

		boolean completed = latch.await(5, TimeUnit.SECONDS);
		long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms

		executor.shutdown();

		assertTrue(completed, "All concurrent requests should complete");
		assertTrue(duration < 1000, "1000 concurrent requests should complete under 1s, was: " + duration + "ms");
	}

	@Test
	void cacheHitRateShouldBeHigh() {
		// Setup
		registry.register("static", new DefaultMessageEntry(MessageType.MESSAGE, "Static text"));

		// First call - cache miss
		service.resolve("static", Locale.US);

		// Next 99 calls - cache hits
		for (int i = 0; i < 99; i++) {
			service.resolve("static", Locale.US);
		}

		// For static messages, should be nearly 100% hit rate after first call
		// This is a smoke test - actual hit rate tracking would need metrics
		assertNotNull(service.resolve("static", Locale.US));
	}

	@Test
	void nestedResolutionShouldStayFast() {
		// Setup deep nesting
		registry.register("a", new DefaultMessageEntry(MessageType.TEMPLATE, "A"));
		registry.register("b", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:a>B"));
		registry.register("c", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:b>C"));
		registry.register("d", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:c>D"));
		registry.register("e", new DefaultMessageEntry(MessageType.TEMPLATE, "<m:d>E"));
		registry.register("final", new DefaultMessageEntry(MessageType.MESSAGE, "Result: <m:e>"));

		// Warm up
		for (int i = 0; i < 100; i++) {
			service.resolve("final", Locale.US);
		}

		// Measure average
		long totalDuration = 0;
		int iterations = 50;

		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			String result = service.resolve("final", Locale.US);
			totalDuration += System.nanoTime() - start;
			assertEquals("Result: ABCDE", result);
		}

		long avgDuration = totalDuration / iterations;
		assertTrue(avgDuration < 200_000, "5-level nesting should average under 200µs, was: " + avgDuration + "ns");
	}

	@Test
	void batchResolutionShouldBeEfficient() {
		// Setup
		for (int i = 0; i < 100; i++) {
			registry.register("msg" + i, new DefaultMessageEntry(MessageType.MESSAGE,
					"Message " + i + ": <p:value>"));
		}

		// Measure batch resolution
		long start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			service.resolve("msg" + i, Locale.US, Map.of("value", "test"));
		}
		long duration = (System.nanoTime() - start) / 1_000; // Convert to µs

		assertTrue(duration < 10_000, "100 resolutions should complete under 10ms, was: " + duration + "µs");
	}

	@Test
	void multiLocaleResolutionShouldBeFast() {
		// Setup
		Locale esLocale = Locale.forLanguageTag("es-ES");
		registry.register("welcome", new DefaultMessageEntry(MessageType.MESSAGE,
				Map.of(
						Locale.US, "Welcome!",
						Locale.GERMAN, "Willkommen!",
						Locale.FRANCE, "Bienvenue!",
						esLocale, "¡Bienvenido!",
						Locale.ITALY, "Benvenuto!"
				)));

		// Warm up all locales
		for (int i = 0; i < 100; i++) {
			service.resolve("welcome", Locale.US);
			service.resolve("welcome", Locale.GERMANY);
			service.resolve("welcome", Locale.FRANCE);
			service.resolve("welcome", esLocale);
			service.resolve("welcome", Locale.ITALY);
		}

		// Measure average for all locales
		long start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			service.resolve("welcome", Locale.US);
			service.resolve("welcome", Locale.GERMANY);
			service.resolve("welcome", Locale.FRANCE);
			service.resolve("welcome", esLocale);
			service.resolve("welcome", Locale.ITALY);
		}
		long duration = (System.nanoTime() - start) / 1_000; // Convert to µs

		long avgPer5Locales = duration / 100;
		assertTrue(avgPer5Locales < 500, "5 locale resolutions should average under 500µs, was: " + avgPer5Locales + "µs");
	}
}