package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Settings;


@Singleton
public class SettingsTemplate implements TemplateProvider<Settings> {
	@Override
	public Settings supply(Settings settings) {
		// Default logger level (2 = INFO)
		settings.setLevel(2);

		// Initialize namespace settings
		Settings.Namespaces namespaces = new Settings.Namespaces();
		namespaces.setAppendToKeys(false);
		settings.setNamespaces(namespaces);

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

		settings.setPerformance(performance);

		// Initialize command settings
		Settings.Commands commands = new Settings.Commands();
		commands.setUseBrigadier(false);
		commands.setUseAsyncCompletions(true);

		settings.setCommands(commands);

		return settings;
	}
}
