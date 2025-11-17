package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.DatabaseConfig;
import me.whereareiam.intercept.type.DatabaseType;

@Singleton
public class DatabaseTemplate implements TemplateProvider<DatabaseConfig> {
	@Override
	public DatabaseConfig supply(DatabaseConfig config) {
		// DatabaseConfig disabled by default
		config.setEnabled(false);

		// Default to PostgreSQL
		config.setType(DatabaseType.POSTGRES);
		config.setHost("localhost");
		config.setPort(5432); // Default PostgreSQL port
		config.setDatabase("intercept");
		config.setUsername("intercept");
		config.setPassword("");

		config.setTablePrefix("intercept_");

		// Initialize HikariCP settings with recommended defaults
		DatabaseConfig.Hikari hikari = new DatabaseConfig.Hikari();
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