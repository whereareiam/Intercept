package me.whereareiam.intercept.common.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Registry;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.model.Settings;

import java.nio.file.Path;

@Singleton
public class SettingsProvider extends DefaultConfigProvider<Settings> {
	@Inject
	public SettingsProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected Settings load() {
		return Config.update(getBasePath().resolve("settings"), Settings.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(SettingsTemplate.class);
	}
}