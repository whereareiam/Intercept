package me.whereareiam.intercept.common.messaging.regex;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.regex.CompiledRegexPattern;
import me.whereareiam.intercept.model.config.Settings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for matching text against message regex patterns.
 * Provides optimization through pattern indexing, literal prefix checking, and caching.
 */
@Singleton
public class RegexMatchingService {
	private final MessageRegistry registry;
	private final MessageService messageService;
	private final Settings settings;

	// Pattern index cache (rebuilt on message reload)
	private volatile List<PatternMatch> patternIndex;

	// Result cache: (text hash, locale) -> resolved message
	private final Map<CacheKey, CachedResult> resultCache;

	@Inject
	public RegexMatchingService(MessageRegistry registry, MessageService messageService, Settings settings) {
		this.registry = registry;
		this.messageService = messageService;
		this.settings = settings;
		this.resultCache = new ConcurrentHashMap<>();
	}

	/**
	 * Try to match text against all registered regex patterns.
	 * Returns the resolved message if a match is found.
	 *
	 * @param text   the text to match
	 * @param locale the locale for message resolution
	 * @return the resolved message, or empty if no match
	 */
	public Optional<String> match(String text, Locale locale) {
		// Check if regex is enabled
		if (!settings.getPerformance().getRegex().isEnabled()) return Optional.empty();

		// Check cache first
		if (settings.getPerformance().getRegex().isCacheResults()) {
			CacheKey cacheKey = new CacheKey(text, locale.toString());
			CachedResult cached = resultCache.get(cacheKey);
			if (cached != null && !cached.isExpired())
				return Optional.ofNullable(cached.result);
		}

		// Build pattern index if not built yet
		if (patternIndex == null) buildPatternIndex();

		// Try to match patterns
		Optional<String> result = tryMatchPatterns(text, locale);

		// Cache result if enabled
		if (settings.getPerformance().getRegex().isCacheResults() && patternIndex != null) {
			CacheKey cacheKey = new CacheKey(text, locale.toString());
			long expireTime = System.currentTimeMillis() + (settings.getPerformance().getRegex().getCacheExpireMinutes() * 60 * 1000L);
			resultCache.put(cacheKey, new CachedResult(result.orElse(null), expireTime));

			// Clean up expired entries if cache is too large
			if (resultCache.size() > settings.getPerformance().getRegex().getCacheSize())
				cleanupCache();
		}

		return result;
	}

	/**
	 * Try to match text against all patterns in the index.
	 */
	private Optional<String> tryMatchPatterns(String text, Locale locale) {
		for (PatternMatch candidate : patternIndex) {
			// Literal prefix optimization
			if (settings.getPerformance().getRegex().isUseLiteralPrefix()) {
				if (!candidate.pattern.hasLiteralPrefix(text)) {
					continue; // Skip pattern if prefix doesn't match
				}
			}

			// Try to match
			long startTime = System.nanoTime();
			Optional<Map<String, Object>> placeholders = candidate.pattern.match(text);
			long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

			// Warn about slow patterns
			if (elapsedMs > settings.getPerformance().getRegex().getWarnSlowPatternsMs()) {
				System.err.println("[Intercept] Slow regex pattern detected for key '" +
						candidate.key + "': took " + elapsedMs + "ms");
			}

			if (placeholders.isPresent()) {
				// Match found! Resolve the message
				return Optional.of(
						messageService.resolve(candidate.key, locale, placeholders.get())
				);
			}
		}

		return Optional.empty();
	}

	/**
	 * Build the pattern index from all registered messages.
	 * This is called once and cached until messages are reloaded.
	 */
	private synchronized void buildPatternIndex() {
		if (patternIndex != null) {
			return; // Already built by another thread
		}

		List<PatternMatch> candidates = new ArrayList<>();

		for (String key : registry.getKeys()) {
			MessageEntry entry = registry.get(key);
			if (entry == null || !entry.hasRegexPatterns()) {
				continue;
			}

			for (CompiledRegexPattern pattern : entry.getRegexPatterns()) {
				candidates.add(new PatternMatch(key, pattern));
			}
		}

		// Sort by priority (higher first)
		candidates.sort(Comparator.comparingInt(pm -> -pm.pattern.getPriority()));

		patternIndex = Collections.unmodifiableList(candidates);
	}

	/**
	 * Clean up expired entries from the result cache.
	 */
	private void cleanupCache() {
		long now = System.currentTimeMillis();
		resultCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
	}

	/**
	 * A pattern match candidate with its key.
	 */
	private record PatternMatch(String key, CompiledRegexPattern pattern) {
	}

	/**
	 * Cache key for regex matching results.
	 */
	private record CacheKey(String text, String locale) {
	}

	/**
	 * Cached regex matching result.
	 */
	private record CachedResult(String result, long expireTime) {
		boolean isExpired() {
			return isExpired(System.currentTimeMillis());
		}

		boolean isExpired(long now) {
			return now > expireTime;
		}
	}
}

