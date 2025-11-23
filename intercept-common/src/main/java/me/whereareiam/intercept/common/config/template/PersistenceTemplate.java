package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.model.config.Persistence;

@Singleton
public class PersistenceTemplate implements TemplateProvider<Persistence> {
	@Override
	public Persistence supply(Persistence config) {
		// Persistence disabled by default
		config.setEnabled(false);

		// Default to PostgreSQL
		config.setType(DatabaseType.POSTGRES);
		config.setHost("localhost");
		config.setPort(5432); // Default PostgreSQL port
		config.setDatabase("intercept");
		config.setUsername("intercept");
		config.setPassword("");

		// Initialize HikariCP settings with recommended defaults
		Persistence.Hikari hikari = new Persistence.Hikari();
		hikari.setPoolName("Intercept");
		hikari.setMaximumPoolSize(10);
		hikari.setMinimumIdle(2);
		hikari.setConnectionTimeout(30000); // 30 seconds
		hikari.setIdleTimeout(600000); // 10 minutes
		hikari.setMaxLifetime(1800000); // 30 minutes
		config.setHikari(hikari);

		return config;
	}
}