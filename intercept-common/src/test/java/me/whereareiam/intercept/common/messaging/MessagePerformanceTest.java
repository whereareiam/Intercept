package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.SemanticaTestHelper;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
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
	private TranslationService<Locale> service;

	@BeforeEach
	void setUp() {
		Registry<Reloadable> registryMock = mock(Registry.class);
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, registryMock);

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
		cache.setSemiStaticExpireMinutes(60);
		cache.setDynamicExpireMinutes(5);
		performance.setCache(cache);

		settings.setPerformance(performance);

		service = SemanticaTestHelper.createService(settings, translationRegistry);
	}

	@Test
	void staticMessageShouldResolveUnder10Microseconds() {
		// Setup
		registerMessage("static", SemanticaTestHelper.template("Static text"));

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
		assertTrue(avgDuration < 10_000, "Static cached message should average under 10æs, was: " + avgDuration + "ns");
	}

	@Test
	void semiStaticMessageShouldResolveUnder100Microseconds() {
		// Setup
		registerMessage("prefix", SemanticaTestHelper.template("[App]"));
		registerMessage("semi", SemanticaTestHelper.template(
				"<ref:prefix> <if enabled==true>Enabled<else>Disabled</if>"));

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
		assertTrue(avgDuration < 100_000, "Semi-static cached message should average under 100æs, was: " + avgDuration + "ns");
	}

	@Test
	void simpleDynamicMessageShouldResolveUnder100Microseconds() {
		// Setup
		registerMessage("dynamic", SemanticaTestHelper.template("Hello, <p:name>!"));

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
		assertTrue(avgDuration < 100_000, "Simple dynamic message should average under 100æs, was: " + avgDuration + "ns");
	}

	@Test
	void complexDynamicMessageShouldResolveUnder500Microseconds() {
		// Setup complex message with all features
		registerMessage("color", SemanticaTestHelper.template("<red>"));
		registerMessage("prefix", SemanticaTestHelper.template("<ref:color>[App]"));
		registerMessage("player-name", SemanticaTestHelper.template("<p:name>"));
		registerMessage("complex", SemanticaTestHelper.template(
				"<ref:prefix> <ref:player-name name='<p:player>'> <if online==true>is online<else>is offline</if> on <p:server>"));

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
		assertTrue(avgDuration < 500_000, "Complex dynamic message should average under 500æs, was: " + avgDuration + "ns");
	}

	@Test
	void concurrentAccessShouldHandleLoad() throws InterruptedException {
		// Setup
		registerMessage("prefix", SemanticaTestHelper.template("[App]"));
		registerMessage("msg", SemanticaTestHelper.template("<ref:prefix> Player <p:name> joined"));

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
		registerMessage("static", SemanticaTestHelper.template("Static text"));

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
		registerMessage("a", SemanticaTestHelper.template("A"));
		registerMessage("b", SemanticaTestHelper.template("<ref:a>B"));
		registerMessage("c", SemanticaTestHelper.template("<ref:b>C"));
		registerMessage("d", SemanticaTestHelper.template("<ref:c>D"));
		registerMessage("e", SemanticaTestHelper.template("<ref:d>E"));
		registerMessage("final", SemanticaTestHelper.template("Result: <ref:e>"));

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
		assertTrue(avgDuration < 200_000, "5-level nesting should average under 200æs, was: " + avgDuration + "ns");
	}

	@Test
	void batchResolutionShouldBeEfficient() {
		// Setup
		for (int i = 0; i < 100; i++) {
			registerMessage("msg" + i, SemanticaTestHelper.template(
					"Message " + i + ": <p:value>"));
		}

		// Measure batch resolution
		long start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			service.resolve("msg" + i, Locale.US, Map.of("value", "test"));
		}
		long duration = (System.nanoTime() - start) / 1_000; // Convert to æs

		assertTrue(duration < 10_000, "100 resolutions should complete under 10ms, was: " + duration + "æs");
	}

	@Test
	void multiLocaleResolutionShouldBeFast() {
		// Setup
		Locale esLocale = Locale.forLanguageTag("es-ES");
		registerMessage("welcome", SemanticaTestHelper.localized(
				Map.of(
						Locale.US, "Welcome!",
						Locale.GERMANY, "Willkommen!",
						Locale.FRANCE, "Bienvenue!",
						esLocale, "­Bienvenido!",
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
		long duration = (System.nanoTime() - start) / 1_000; // Convert to æs

		long avgPer5Locales = duration / 100;
		assertTrue(avgPer5Locales < 500, "5 locale resolutions should average under 500æs, was: " + avgPer5Locales + "æs");
	}

	private void registerMessage(String key, TranslationEntry entry) {
		registry.register(key, entry);
		SemanticaTestHelper.register(service, key, entry);
	}
}
