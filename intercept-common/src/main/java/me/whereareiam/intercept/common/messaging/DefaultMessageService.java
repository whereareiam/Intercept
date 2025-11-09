package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.cache.CacheKey;
import me.whereareiam.intercept.common.messaging.cache.CacheLevel;
import me.whereareiam.intercept.common.messaging.cache.CacheStrategy;
import me.whereareiam.intercept.common.messaging.cache.MessageCache;
import me.whereareiam.intercept.common.messaging.processor.MessageReferenceProcessor;
import me.whereareiam.intercept.common.messaging.processor.PlaceholderProcessor;
import me.whereareiam.intercept.common.messaging.processor.TemplateProcessor;
import me.whereareiam.intercept.common.messaging.processor.conditional.ConditionalProcessor;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.MessageRequest;
import me.whereareiam.intercept.model.config.Settings;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of MessageService.
 * Orchestrates the message resolution pipeline with caching support.
 */
@Singleton
public class DefaultMessageService implements MessageService {
	private final MessageRegistry registry;
	private final TemplateProcessor templateProcessor;
	private final MessageReferenceProcessor referenceProcessor;
	private final ConditionalProcessor conditionalProcessor;
	private final PlaceholderProcessor placeholderProcessor;
	private final CacheStrategy cacheStrategy;
	private final MessageCache cache;
	private final Settings settings;

	@Inject
	public DefaultMessageService(
			MessageRegistry registry,
			Settings settings
	) {
		this.registry = registry;
		this.settings = settings;
		this.templateProcessor = new TemplateProcessor(registry);
		this.referenceProcessor = new MessageReferenceProcessor(registry);
		this.conditionalProcessor = new ConditionalProcessor();
		this.placeholderProcessor = new PlaceholderProcessor();
		this.cacheStrategy = new CacheStrategy();

		// Initialize cache with performance settings
		Settings.Performance.Cache cacheConfig = settings.getPerformance().getCache();
		this.cache = new MessageCache(
				cacheConfig.getSemiStaticSize(),
				cacheConfig.getDynamicSize(),
				cacheConfig.getSemiStaticExpireMinutes(),
				cacheConfig.getDynamicExpireMinutes()
		);
	}

	@Override
	public String resolve(MessageRequest request) {
		return resolve(request.getKey(), request.getLocale(), request.getPlaceholders());
	}

	@Override
	public String resolve(String key, Locale locale, Map<String, Object> placeholders) {
		MessageEntry entry = registry.get(key);
		if (entry == null) return key;

		// Get text for locale with fallback
		String localeString = locale.toString();
		String defaultLocale = settings.getPerformance() != null ? settings.getLocale().toString() : null;
		String text = entry.hasTranslations()
				? entry.getText(localeString, defaultLocale, key)
				: entry.getText();

		// Check cache if enabled
		if (settings.getPerformance().getCache().isEnabled()) {
			CacheLevel level = cacheStrategy.classify(text);

			// For static and semi-static, check cache
			if (level != CacheLevel.DYNAMIC) {
				CacheKey cacheKey = new CacheKey(key, localeString, placeholders);
				String cached = cache.get(cacheKey, level);
				if (cached != null) return cached;

				// Not in cache, resolve and cache it
				String resolved = processMessage(text, localeString, placeholders);
				cache.put(cacheKey, resolved, level);

				return resolved;
			}
		}

		// Dynamic or cache disabled - process without caching
		return processMessage(text, localeString, placeholders);
	}

	private String processMessage(String text, String locale, Map<String, Object> placeholders) {
		// Processing pipeline (order matters!)
		// 1. Templates (expand templates)
		text = templateProcessor.process(text, locale);

		// 2. Message References (resolve nested messages)
		text = referenceProcessor.process(text, locale);

		// 3. Conditionals (evaluate conditions)
		text = conditionalProcessor.process(text, placeholders);

		// 4. Placeholders (final placeholder resolution)
		text = placeholderProcessor.process(text, placeholders);

		return text;
	}

	@Override
	public String resolve(String key, Locale locale) {
		return resolve(key, locale, Map.of());
	}

	@Override
	public boolean exists(String key) {
		return registry.exists(key);
	}

	@Override
	public Set<String> getAvailableLocales(String key) {
		MessageEntry entry = registry.get(key);
		return entry != null ? entry.getLocales() : Set.of();
	}
}