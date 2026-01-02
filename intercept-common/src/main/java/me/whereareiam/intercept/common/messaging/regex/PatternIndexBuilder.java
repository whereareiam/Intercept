package me.whereareiam.intercept.common.messaging.regex;

import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Builds and manages the pattern index for regex matching.
 * The index is sorted by priority (higher priority first).
 */
@RequiredArgsConstructor
class PatternIndexBuilder {
	private final InterceptionRegistry registry;
	private volatile List<PatternMatch> patternIndex;

	/**
	 * Build the pattern index from all registered messages.
	 * This is called once and cached until messages are reloaded.
	 */
	synchronized void buildPatternIndex() {
		if (patternIndex != null)
			return; // Already built by another thread

		List<PatternMatch> candidates = new ArrayList<>();

		for (String key : registry.getKeys()) {
			for (CompiledRegexPattern pattern : registry.get(key)) {
				candidates.add(new PatternMatch(key, pattern));
			}
		}

		// Sort by priority (higher first)
		candidates.sort(Comparator.comparingInt(pm -> -pm.pattern.getPriority()));

		patternIndex = Collections.unmodifiableList(candidates);
	}

	/**
	 * Get the current pattern index. Builds it if not already built.
	 */
	List<PatternMatch> getPatternIndex() {
		if (patternIndex == null)
			buildPatternIndex();

		return patternIndex;
	}

	/**
	 * Clear the pattern index (called on reload).
	 */
	void clear() {
		patternIndex = null;
	}

	/**
	 * A pattern match candidate with its key.
	 */
	@RequiredArgsConstructor
	static class PatternMatch {
		final String key;
		final CompiledRegexPattern pattern;
	}
}

