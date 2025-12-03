package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.CommandsTemplate;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.registry.base.Registry;

import java.nio.file.Path;

@Singleton
public class CommandsProvider extends DefaultConfigProvider<Commands> {
	@Inject
	public CommandsProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected Commands load() {
		return Config.update(getBasePath().resolve("commands"), Commands.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(CommandsTemplate.class);
	}
}