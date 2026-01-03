package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.RebuildMetrics;
import me.whereareiam.semantica.translation.TranslationService;

import java.util.Locale;
import java.util.Set;

/**
 * Service that orchestrates message file loading.
 * Handles initialization and loading of all message files from the messages directory.
 */
@Singleton
public class MessageLifecycleService implements Reloadable {
	private final MessageDataService messageDataService;
	private final Provider<Settings> settingsProvider;
	private final TranslationService<Locale> translationService;
	private final Set<MessageLifecycleHook> lifecycleHooks;

	@Inject
	public MessageLifecycleService(
			MessageDataService messageDataService,
			Provider<Settings> settingsProvider,
			TranslationService<Locale> translationService,
			Set<MessageLifecycleHook> lifecycleHooks,
			Registry<Reloadable> reloadableRegistry
	) {
		this.messageDataService = messageDataService;
		this.settingsProvider = settingsProvider;
		this.translationService = translationService;
		this.lifecycleHooks = lifecycleHooks;

		reloadableRegistry.register(this);
	}

	/**
	 * Initialize the messages system by loading all message files.
	 */
	public void initialize() {
		translationService.unregisterByPrefix("");
		runBeforeLoad();

		// Use MessageDataService to load all files
		messageDataService.initialize();

		applyPostLoadOptimizations();
		runAfterLoad();
	}

	@Override
	public void reload() {
		translationService.unregisterByPrefix("");
		runBeforeLoad();
		messageDataService.reload();
		applyPostLoadOptimizations();
		runAfterLoad();
	}

	private void applyPostLoadOptimizations() {
		Settings settings = settingsProvider.get();

		boolean shouldBuildGraph = settings.getPerformance().isBuildDependencyGraph();
		boolean shouldPrerender = settings.getPerformance().isPrerenderStatic()
				&& settings.getPerformance().getCache().isEnabled();

		if (shouldBuildGraph || shouldPrerender) {
			RebuildMetrics stats = translationService.rebuild();
			Logger.info("Finished compiling in %d ms", stats.getDurationMs());
			if (stats.getCircularDependencies() > 0)
				Logger.warn("Detected %d circular dependencies", stats.getCircularDependencies());
			Logger.debug("Pre-rendered %d static messages", stats.getPrerenderedEntries());
		}
	}

	private void runBeforeLoad() {
		if (lifecycleHooks == null || lifecycleHooks.isEmpty()) return;
		for (MessageLifecycleHook hook : lifecycleHooks) {
			hook.beforeLoad();
		}
	}

	private void runAfterLoad() {
		if (lifecycleHooks == null || lifecycleHooks.isEmpty()) return;
		for (MessageLifecycleHook hook : lifecycleHooks) {
			hook.afterLoad();
		}
	}
}
