package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.DatabaseTemplate;
import me.whereareiam.intercept.model.config.DatabaseConfig;
import me.whereareiam.intercept.registry.Registry;

import java.nio.file.Path;

@Singleton
public class DatabaseProvider extends DefaultConfigProvider<DatabaseConfig> {
	@Inject
	public DatabaseProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected DatabaseConfig load() {
		return Config.update(getBasePath().resolve("database"), DatabaseConfig.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(DatabaseTemplate.class);
	}
}

