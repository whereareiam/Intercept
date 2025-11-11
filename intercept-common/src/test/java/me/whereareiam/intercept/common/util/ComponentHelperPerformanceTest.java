package me.whereareiam.intercept.common.util;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance test to verify caching effectiveness.
 * These tests demonstrate the performance improvements from caching in TagParser.
 */
class ComponentHelperPerformanceTest {
	@Test
	void performanceCachingEffectiveness() {
		String text = "Welcome <lang key=\"message\" name=\"Steve\" rank=\"Admin\"> to the server!";
		String tagFormat = "<lang>";

		// Warm up
		for (int i = 0; i < 100; i++) {
			TagParser.extractTags(text, tagFormat);
		}

		// Measure with caching (subsequent calls)
		long start = System.nanoTime();
		int iterations = 10000;
		for (int i = 0; i < iterations; i++) {
			TagParser.extractTags(text, tagFormat);
		}
		long cachedTime = System.nanoTime() - start;

		double avgMs = cachedTime / 1_000_000.0 / iterations;

		System.out.println("=== TagParser Performance Test ===");
		System.out.println("Total time: " + (cachedTime / 1_000_000.0) + " ms");
		System.out.println("Iterations: " + iterations);
		System.out.println("Average per call: " + String.format("%.4f", avgMs) + " ms");
		System.out.println("Calls per second: " + String.format("%.0f", 1000.0 / avgMs));

		// Performance should be very fast with caching (< 0.01ms per call)
		assertTrue(avgMs < 0.1, "Average call should be under 0.1ms with caching, was: " + avgMs + "ms");
	}

	@Test
	void performanceWithDifferentFormats() {
		String[] formats = {"<lang>", "[l]", "{tr}"};
		String[] texts = {
				"Hello <lang key=\"message\">",
				"Hello [l key=\"message\"]",
				"Hello {tr key=\"message\"}"
		};

		// First pass - builds cache for all formats
		long firstPassStart = System.nanoTime();
		for (int i = 0; i < formats.length; i++) {
			TagParser.extractTags(texts[i], formats[i]);
		}
		long firstPassTime = System.nanoTime() - firstPassStart;

		// Second pass - all formats cached
		long secondPassStart = System.nanoTime();
		int iterations = 10000;
		for (int i = 0; i < iterations; i++) {
			for (int j = 0; j < formats.length; j++) {
				TagParser.extractTags(texts[j], formats[j]);
			}
		}
		long secondPassTime = System.nanoTime() - secondPassStart;

		double avgPerCall = secondPassTime / 1_000_000.0 / iterations / formats.length;

		System.out.println("=== Multiple Format Performance Test ===");
		System.out.println("First pass (cache build): " + (firstPassTime / 1_000_000.0) + " ms");
		System.out.println("Second pass (" + (iterations * formats.length) + " calls): " + (secondPassTime / 1_000_000.0) + " ms");
		System.out.println("Average per call: " + String.format("%.4f", avgPerCall) + " ms");

		assertTrue(avgPerCall < 0.1, "Average call should be under 0.1ms, was: " + avgPerCall + "ms");
	}

	@Test
	void performanceWithComplexTags() {
		String text = "<lang key=\"player.info\" name=\"Steve\" rank=\"Admin\" location=\"Spawn\" health=\"20\" level=\"99\">";
		String tagFormat = "<lang>";

		// Warm up
		for (int i = 0; i < 100; i++) {
			TagParser.extractTags(text, tagFormat);
		}

		// Measure
		long start = System.nanoTime();
		int iterations = 10000;
		for (int i = 0; i < iterations; i++) {
			List<TagParser.TagData> tags = TagParser.extractTags(text, tagFormat);
			// Verify it's working
			if (i == 0) {
				assertEquals(1, tags.size());
				assertEquals(5, tags.get(0).placeholders().size());
			}
		}
		long time = System.nanoTime() - start;

		double avgMs = time / 1_000_000.0 / iterations;

		System.out.println("=== Complex Tag Performance Test ===");
		System.out.println("Total time: " + (time / 1_000_000.0) + " ms");
		System.out.println("Iterations: " + iterations);
		System.out.println("Average per call: " + String.format("%.4f", avgMs) + " ms");

		assertTrue(avgMs < 0.2, "Complex tag parsing should be under 0.2ms, was: " + avgMs + "ms");
	}

	@Test
	void performanceContainsTag() {
		Component component = Component.text("Welcome <lang key=\"message\">");
		String plainText = ComponentHelper.extractPlainText(component);
		String tagFormat = "<lang>";

		// Warm up
		for (int i = 0; i < 100; i++) {
			TagParser.containsTag(plainText, tagFormat);
		}

		// Measure
		long start = System.nanoTime();
		int iterations = 100000;
		for (int i = 0; i < iterations; i++) {
			TagParser.containsTag(plainText, tagFormat);
		}
		long time = System.nanoTime() - start;

		double avgMs = time / 1_000_000.0 / iterations;

		System.out.println("=== ContainsTag Performance Test ===");
		System.out.println("Total time: " + (time / 1_000_000.0) + " ms");
		System.out.println("Iterations: " + iterations);
		System.out.println("Average per call: " + String.format("%.6f", avgMs) + " ms");
		System.out.println("Calls per second: " + String.format("%.0f", 1000.0 / avgMs));

		assertTrue(avgMs < 0.01, "ContainsTag should be under 0.01ms, was: " + avgMs + "ms");
	}

	@Test
	void performanceMultipleTagsInText() {
		String text = "<lang key=\"prefix\"> Welcome <lang key=\"player.join\" name=\"Steve\"> to <lang key=\"server.name\">";
		String tagFormat = "<lang>";

		// Warm up
		for (int i = 0; i < 100; i++) {
			TagParser.extractTags(text, tagFormat);
		}

		// Measure
		long start = System.nanoTime();
		int iterations = 10000;
		for (int i = 0; i < iterations; i++) {
			List<TagParser.TagData> tags = TagParser.extractTags(text, tagFormat);
			if (i == 0) {
				assertEquals(3, tags.size());
			}
		}
		long time = System.nanoTime() - start;

		double avgMs = time / 1_000_000.0 / iterations;

		System.out.println("=== Multiple Tags Performance Test ===");
		System.out.println("Total time: " + (time / 1_000_000.0) + " ms");
		System.out.println("Iterations: " + iterations);
		System.out.println("Average per call: " + String.format("%.4f", avgMs) + " ms");
		System.out.println("Tags extracted per second: " + String.format("%.0f", 3 * 1000.0 / avgMs));

		assertTrue(avgMs < 0.2, "Multiple tag extraction should be under 0.2ms, was: " + avgMs + "ms");
	}
}
