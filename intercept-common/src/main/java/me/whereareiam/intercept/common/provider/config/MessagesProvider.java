package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.MessagesTemplate;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.registry.base.Registry;

import java.nio.file.Path;

@Singleton
public class MessagesProvider extends DefaultConfigProvider<Messages> {
	@Inject
	public MessagesProvider(@Named("dataPath") Path dataPath, Registry<Reloadable> registry) {
		super(dataPath, registry);
	}

	@Override
	protected Messages load() {
		return Config.update(getBasePath().resolve("messages"), Messages.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(MessagesTemplate.class);
	}
}