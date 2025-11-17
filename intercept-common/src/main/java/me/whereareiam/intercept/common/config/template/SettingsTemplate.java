package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.Event;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.type.EventPriority;
import me.whereareiam.intercept.type.PlatformType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Singleton
public class SettingsTemplate implements TemplateProvider<Settings> {
	@Override
	public Settings supply(Settings settings) {
		// Default logger level (2 = INFO)
		settings.setLevel(2);

		// Default locale for messages
		settings.setLocale(Locale.US);

		// Initialize serialization settings
		Settings.Serialization serialization = new Settings.Serialization();
		serialization.setType("MINIMESSAGE");
		serialization.setEnableLegacyColors(false);
		settings.setSerialization(serialization);

		// Initialize updater settings
		Settings.Updater updater = new Settings.Updater();
		updater.setCheckForUpdates(true);
		updater.setWarnAboutUpdates(true);
		updater.setWarnAboutLocalBuilds(true);
		updater.setWarnAboutDevBuilds(true);
		updater.setInterval(60); // 60 minutes

		settings.setUpdater(updater);

		// Initialize performance settings
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

		Settings.Performance.Regex regex = new Settings.Performance.Regex();
		regex.setEnabled(true);
		regex.setTimeoutMs(100);
		regex.setUseLiteralPrefix(true);
		regex.setCacheResults(true);
		regex.setCacheSize(1000);
		regex.setCacheExpireMinutes(10);
		regex.setMaxPatternComplexity(1000);
		regex.setWarnSlowPatternsMs(50);
		performance.setRegex(regex);

		settings.setPerformance(performance);

		// Initialize command settings
		Settings.Commands commands = new Settings.Commands();
		commands.setUseBrigadier(false);
		commands.setUseAsyncCompletions(true);

		settings.setCommands(commands);

		configureListeners(settings);

		return settings;
	}

	private void configureListeners(Settings settings) {
		Settings.Listeners listeners = new Settings.Listeners();
		if (PlatformType.isAtLeast(PlatformType.BUKKIT))
			listeners.setEvents(getPrioritiesForBukkit());

		settings.setListeners(listeners);
	}

	private Map<String, Event> getPrioritiesForBukkit() {
		Map<String, Event> priorities = new HashMap<>();

		Event event = Event.builder().register(true).priority(EventPriority.LOWEST).build();

		priorities.put("org.bukkit.event.player.PlayerQuitEvent", event);

		return priorities;
	}
}