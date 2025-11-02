package me.whereareiam.intercept.common.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.writer.ConfigWriter;
import me.whereareiam.intercept.config.ConfigurationTypeResolver;
import me.whereareiam.intercept.type.ConfigurationType;

@Singleton
public class ConfiguraBootstrap {
	@Inject
	public ConfiguraBootstrap(ConfigurationTypeResolver resolver) {
		// Resolve the preferred configuration format
		ConfigurationType type = resolver.getConfigurationType();
		Format format = (type == ConfigurationType.JSON) ? Format.JSON : Format.YAML;

		// Configure global reader/writer with chosen format
		ConfigReader reader = Config.reader(format);
		ConfigWriter writer = Config.writer(format);
		Config.setReader(reader);
		Config.setWriter(writer);
	}
}