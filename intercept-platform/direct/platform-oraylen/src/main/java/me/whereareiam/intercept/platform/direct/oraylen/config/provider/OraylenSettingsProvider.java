package me.whereareiam.intercept.platform.direct.oraylen.config.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.provider.config.DefaultConfigProvider;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;

import java.nio.file.Path;

@Singleton
public class OraylenSettingsProvider extends DefaultConfigProvider<Settings> {
	@Inject
	public OraylenSettingsProvider(@Named("extensionPath") Path extensionPath, Registry<Reloadable> registry) {
		super(extensionPath, registry);
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
