package me.whereareiam.intercept.common.messaging.regex;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.model.regex.MatchDetails;
import me.whereareiam.semantica.translation.TranslationService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the actual pattern matching logic against a pattern index.
 */
@RequiredArgsConstructor
class PatternMatcher {
	private final TranslationService<Locale> translationService;
	private final Provider<Settings> settingsProvider;

	/**
	 * Try to match text against all patterns in the index.
	 */
	Optional<String> tryMatchPatterns(String text, Locale locale, List<PatternIndexBuilder.PatternMatch> patternIndex) {
		Settings settings = settingsProvider.get();
		for (PatternIndexBuilder.PatternMatch candidate : patternIndex) {
			// Literal prefix optimization
			if (settings.getPerformance().getRegex().isUseLiteralPrefix()) {
				if (!candidate.pattern.hasLiteralPrefix(text)) {
					Logger.debug("[Regex] Pattern \"%s\" (key: %s) - literal prefix check failed, skipped",
							candidate.pattern.getRegex(), candidate.key);
					continue; // Skip pattern if prefix doesn't match
				}
			}

			// Try to match
			long startTime = System.nanoTime();
			Optional<CompiledRegexPattern.MatchResult> matchResult = candidate.pattern.match(text);
			long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

			// Warn about slow patterns
			if (elapsedMs > settings.getPerformance().getRegex().getWarnSlowPatternsMs()) {
				System.err.println("[Intercept] Slow regex pattern detected for key '" +
						candidate.key + "': took " + elapsedMs + "ms");
			}

			if (matchResult.isPresent()) {
				CompiledRegexPattern.MatchResult result = matchResult.get();
				Map<String, Object> placeholders = result.placeholders();
				// Match found! Resolve the message
				Logger.debug("[Regex] Pattern \"%s\" (key: %s) - MATCHED with placeholders: %s",
						candidate.pattern.getRegex(), candidate.key, placeholders);

				String resolved = translationService.resolve(candidate.key, locale, placeholders);

				String finalText = resolved;
				if (candidate.pattern.isReplaceMatched())
					finalText = text.substring(0, result.start()) +
							resolved +
							text.substring(result.end());

				return Optional.of(finalText);
			}

			Logger.debug("[Regex] Pattern \"%s\" (key: %s) - no match",
					candidate.pattern.getRegex(), candidate.key);
		}

		Logger.debug("[Regex] No patterns matched for message: \"%s\"", text);
		return Optional.empty();
	}

	/**
	 * Try to match text against all patterns in the index and return match details.
	 */
	Optional<MatchDetails> tryMatchPatternsWithDetails(
			String text,
			Locale locale,
			List<PatternIndexBuilder.PatternMatch> patternIndex
	) {
		Settings settings = settingsProvider.get();
		for (PatternIndexBuilder.PatternMatch candidate : patternIndex) {
			// Literal prefix optimization
			if (settings.getPerformance().getRegex().isUseLiteralPrefix()) {
				if (!candidate.pattern.hasLiteralPrefix(text)) {
					Logger.debug("[Regex] Pattern \"%s\" (key: %s) - literal prefix check failed, skipped",
							candidate.pattern.getRegex(), candidate.key);
					continue; // Skip pattern if prefix doesn't match
				}
			}

			// Try to match
			long startTime = System.nanoTime();
			Optional<CompiledRegexPattern.MatchResult> matchResult = candidate.pattern.match(text);
			long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

			// Warn about slow patterns
			if (elapsedMs > settings.getPerformance().getRegex().getWarnSlowPatternsMs())
				Logger.warn("Slow regex pattern detected for key '" +
						candidate.key + "': took " + elapsedMs + "ms");

			if (matchResult.isPresent()) {
				CompiledRegexPattern.MatchResult result = matchResult.get();
				Map<String, Object> placeholders = result.placeholders();
				// Match found! Resolve the message
				Logger.debug("[Regex] Pattern \"%s\" (key: %s) - MATCHED with placeholders: %s",
						candidate.pattern.getRegex(), candidate.key, placeholders);

				String resolved = translationService.resolve(candidate.key, locale, placeholders);
				boolean replaceMatched = candidate.pattern.isReplaceMatched();

				return Optional.of(new MatchDetails(resolved, result.start(), result.end(), replaceMatched));
			}

			Logger.debug("[Regex] Pattern \"%s\" (key: %s) - no match",
					candidate.pattern.getRegex(), candidate.key);
		}

		Logger.debug("[Regex] No patterns matched for message: \"%s\"", text);
		return Optional.empty();
	}
}
