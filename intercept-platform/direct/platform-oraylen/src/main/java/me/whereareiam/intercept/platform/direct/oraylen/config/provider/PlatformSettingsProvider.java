package me.whereareiam.intercept.platform.direct.oraylen.config.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.provider.config.DefaultConfigProvider;
import me.whereareiam.intercept.platform.direct.oraylen.config.PlatformSettings;
import me.whereareiam.intercept.platform.direct.oraylen.config.template.PlatformSettingsTemplate;
import me.whereareiam.intercept.registry.base.Registry;

import java.nio.file.Path;

@Singleton
public class PlatformSettingsProvider extends DefaultConfigProvider<PlatformSettings> {
	@Inject
	public PlatformSettingsProvider(
			@Named("extensionPath") Path extensionPath,
			Registry<Reloadable> registry
	) {
		super(extensionPath, registry);
	}

	@Override
	protected PlatformSettings load() {
		return Config.update(getBasePath().resolve("platform"), PlatformSettings.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(PlatformSettingsTemplate.class);
	}
}
