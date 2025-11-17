package me.whereareiam.intercept.common.messaging.regex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Registry;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.regex.MatchDetails;

import java.util.Locale;
import java.util.Optional;

/**
 * Default implementation of RegexMatchingService.
 * Orchestrates pattern indexing, matching, and caching.
 */
@Singleton
public class DefaultRegexMatchingService implements RegexMatchingService, Reloadable {
	private final PatternIndexBuilder patternIndexBuilder;
	private final PatternMatcher patternMatcher;
	private final ResultCache resultCache;
	private final Provider<Settings> settingsProvider;

	@Inject
	public DefaultRegexMatchingService(
			MessageRegistry registry,
			MessageService messageService,
			Provider<Settings> settingsProvider,
			Registry<Reloadable> reloadableRegistry
	) {
		this.settingsProvider = settingsProvider;
		this.patternIndexBuilder = new PatternIndexBuilder(registry);
		this.patternMatcher = new PatternMatcher(messageService, settingsProvider);
		this.resultCache = new ResultCache(settingsProvider);

		reloadableRegistry.register(this);
	}

	@Override
	public Optional<String> match(String text, Locale locale) {
		Settings settings = settingsProvider.get();
		// Check if regex is enabled
		if (!settings.getPerformance().getRegex().isEnabled()) return Optional.empty();

		Logger.debug("[Regex] Received message to match: \"%s\"", text);

		// Check cache first
		Optional<String> cached = resultCache.get(text, locale.toString());
		if (cached.isPresent()) {
			Logger.debug("[Regex] Cache hit for message: \"%s\"", text);
			return cached;
		}

		// Get pattern index (builds if needed)
		var patternIndex = patternIndexBuilder.getPatternIndex();

		// Try to match patterns
		Optional<String> result = patternMatcher.tryMatchPatterns(text, locale, patternIndex);

		// Cache result if enabled
		resultCache.put(text, locale.toString(), result);

		return result;
	}

	@Override
	public Optional<MatchDetails> matchWithDetails(String text, Locale locale) {
		Settings settings = settingsProvider.get();
		// Check if regex is enabled
		if (!settings.getPerformance().getRegex().isEnabled()) return Optional.empty();

		Logger.debug("[Regex] Received message to match: \"%s\"", text);

		// Get pattern index (builds if needed)
		var patternIndex = patternIndexBuilder.getPatternIndex();

		// Try to match patterns
		return patternMatcher.tryMatchPatternsWithDetails(text, locale, patternIndex);
	}

	@Override
	public void reload() {
		patternIndexBuilder.clear();
		resultCache.clear();
	}
}

