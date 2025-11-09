package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.common.messaging.cache.CacheKey;
import me.whereareiam.intercept.common.messaging.cache.CacheLevel;
import me.whereareiam.intercept.common.messaging.cache.CacheStrategy;
import me.whereareiam.intercept.common.messaging.cache.MessageCache;
import me.whereareiam.intercept.common.messaging.loader.MessageFileData;
import me.whereareiam.intercept.common.messaging.loader.MessageFileLoader;
import me.whereareiam.intercept.common.messaging.loader.MessageFileScanner;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service that orchestrates message file loading.
 * Handles initialization and loading of all message files from the messages directory.
 */
@Singleton
public class MessagesService {
	private final Path messagesPath;
	private final DefaultMessageRegistry registry;
	private final MessageFileScanner scanner;
	private final MessageFileLoader loader;
	private final Provider<Settings> settingsProvider;
	private final MessageService messageService;
	private final DependencyGraph dependencyGraph;
	private final CacheStrategy cacheStrategy;

	@Inject
	public MessagesService(
			@Named("messagesPath") Path messagesPath,
			DefaultMessageRegistry registry,
			Provider<Settings> settingsProvider,
			MessageService messageService
	) {
		this.messagesPath = messagesPath;
		this.registry = registry;
		this.settingsProvider = settingsProvider;
		this.messageService = messageService;
		this.scanner = new MessageFileScanner(Config.getDefaultReader().getFormat());

		TextProcessor textProcessor = new TextProcessor();
		this.loader = new MessageFileLoader(textProcessor, registry);

		this.dependencyGraph = new DependencyGraph();
		this.cacheStrategy = new CacheStrategy();
	}

	/**
	 * Initialize the messages system by loading all message files.
	 */
	public void initialize() {
		if (!Files.exists(messagesPath)) {
			Logger.warn("Messages directory does not exist: %s", messagesPath);
			return;
		}

		// Scan for all message files
		List<Path> files = scanner.scanDirectory(messagesPath);
		Logger.debug("Found %d message files", files.size());

		// Load each file
		for (Path file : files) {
			try {
				loadFile(file);
			} catch (Exception e) {
				Logger.severe("Failed to load message file %s: %s", file, e.getMessage());
				e.printStackTrace();
			}
		}

		// Post-loading optimizations
		Settings settings = settingsProvider.get();

		boolean shouldBuildGraph = settings.getPerformance().isBuildDependencyGraph();
		boolean shouldPrerender = settings.getPerformance().isPrerenderStatic() && settings.getPerformance().getCache().isEnabled();

		if (shouldBuildGraph || shouldPrerender) {
			Logger.info("Starting compilation...");
			long startTime = System.currentTimeMillis();

			if (shouldBuildGraph) buildDependencyGraph();
			if (shouldPrerender) prerenderStaticMessages();

			long duration = System.currentTimeMillis() - startTime;
			Logger.info("Finished compiling in %d ms", duration);
		}
	}

	private void buildDependencyGraph() {
		Logger.debug("Building dependency graph...");

		@SuppressWarnings("unchecked")
		Map<String, DefaultMessageEntry> entries = (Map<String, DefaultMessageEntry>) (Map<?, ?>) registry.getAllEntries();
		dependencyGraph.build(entries);

		// Log warnings for circular dependencies
		for (String key : entries.keySet())
			if (dependencyGraph.hasCircularDependency(key))
				Logger.warn("Circular dependency detected for message key: %s", key);

		Logger.debug("Dependency graph built for %d entries", entries.size());
	}

	private void prerenderStaticMessages() {
		Settings settings = settingsProvider.get();
		Settings.Performance.Cache cacheConfig = settings.getPerformance().getCache();

		MessageCache cache = new MessageCache(
				cacheConfig.getSemiStaticSize(),
				cacheConfig.getDynamicSize(),
				cacheConfig.getSemiStaticExpireMinutes(),
				cacheConfig.getDynamicExpireMinutes()
		);

		int preRendered = 0;
		for (Map.Entry<String, MessageEntry> entry : registry.getAllEntries().entrySet()) {
			String key = entry.getKey();
			MessageEntry messageEntry = entry.getValue();

			// Get text for classification
			String text = messageEntry.hasTranslations()
					? messageEntry.getText(settings.getLocale().toString())
					: messageEntry.getText();

			if (text == null) continue;

			// Only pre-render fully static messages
			CacheLevel level = cacheStrategy.classify(text);
			if (level == CacheLevel.STATIC) {
				// Pre-render for all available locales
				if (messageEntry.hasTranslations()) {
					for (String localeString : messageEntry.getLocales()) {
						Locale locale = Locale.forLanguageTag(localeString.replace('_', '-'));
						String resolved = messageService.resolve(key, locale, Map.of());
						if (resolved != null) {
							cache.put(new CacheKey(key, localeString, Map.of()), resolved, CacheLevel.STATIC);
							preRendered++;
						}
					}
					continue;
				}

				String resolved = messageService.resolve(key, settings.getLocale(), Map.of());
				if (resolved != null) {
					cache.put(new CacheKey(key, settings.getLocale().toString(), Map.of()), resolved, CacheLevel.STATIC);
					preRendered++;
				}
			}
		}

		Logger.debug("Pre-rendered %d static messages", preRendered);
	}

	private void loadFile(Path file) {
		String keyPrefix = scanner.buildKeyPrefix(messagesPath, file);
		Logger.debug("Loading file: %s with key prefix: %s", file.getFileName(), keyPrefix);

		// Read file data with Configura - Jackson handles MessageType enum aliases automatically
		MessageFileData data = Config.load(file, MessageFileData.class);

		// Load into registry
		loader.loadFromData(keyPrefix, data);
	}
}