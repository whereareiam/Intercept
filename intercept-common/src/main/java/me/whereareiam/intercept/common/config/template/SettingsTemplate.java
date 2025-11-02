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

		// Initialize updater settings
		Settings.Updater updater = new Settings.Updater();
		updater.setCheckForUpdates(true);
		updater.setWarnAboutUpdates(true);
		updater.setWarnAboutLocalBuilds(true);
		updater.setWarnAboutDevBuilds(true);
		updater.setInterval(60); // 60 minutes

		settings.setUpdater(updater);

		return settings;
	}
}