package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Settings;

import java.util.List;


@Singleton
public class SettingsTemplate implements TemplateProvider<Settings> {
	@Override
	public Settings supply(Settings settings) {
		// Default logger level (2 = INFO)
		settings.setLevel(2);

		// Configure translation settings
		Settings.Translation translation = new Settings.Translation();
		Settings.Translation.Tag tag = new Settings.Translation.Tag();
		tag.setFormat("<lang>");
		tag.setAutoProcess(false); // Disabled by default
		translation.setTag(tag);

		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setAppendToKeys(true);
		namespaces.setExtra(List.of());
		namespaces.setLoad(List.of());
		translation.setNamespaces(namespaces);

		settings.setTranslation(translation);

		// Initialize updater settings
		Settings.Updater updater = new Settings.Updater();
		updater.setCheckForUpdates(true);
		updater.setWarnAboutUpdates(true);
		updater.setWarnAboutLocalBuilds(true);
		updater.setWarnAboutDevBuilds(true);
		updater.setInterval(1); // 1 hour

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
