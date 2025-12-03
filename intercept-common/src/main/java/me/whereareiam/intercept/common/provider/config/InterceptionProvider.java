package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.InterceptionConfigTemplate;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.registry.base.Registry;

import java.nio.file.Path;

@Singleton
public class InterceptionProvider extends DefaultConfigProvider<Interception> {
	@Inject
	public InterceptionProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected Interception load() {
		return Config.update(getBasePath().resolve("interceptions"), Interception.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(InterceptionConfigTemplate.class);
	}
}