package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.PersistenceTemplate;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.registry.Registry;

import java.nio.file.Path;

@Singleton
public class PersistenceProvider extends DefaultConfigProvider<Persistence> {
	@Inject
	public PersistenceProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected Persistence load() {
		return Config.update(getBasePath().resolve("database"), Persistence.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(PersistenceTemplate.class);
	}
}

