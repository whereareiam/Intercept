package me.whereareiam.intercept.adapter.database.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.intercept.type.PersistenceType;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;

/**
 * Configuration class to store PersistenceType in Jdbi's config registry.
 * This allows handlers to access the database type at runtime.
 */
@NoArgsConstructor
@AllArgsConstructor
public final class DialectConfig implements JdbiConfig<DialectConfig> {
	@Setter
	private PersistenceType persistenceType;

	/**
	 * Gets the PersistenceType from the config registry.
	 *
	 * @param config the Jdbi config registry
	 * @return the PersistenceType, or null if not set
	 */
	public static PersistenceType getPersistenceType(ConfigRegistry config) {
		DialectConfig dbConfig = config.get(DialectConfig.class);

		return dbConfig != null ? dbConfig.persistenceType : null;
	}

	@Override
	public DialectConfig createCopy() {
		return new DialectConfig(persistenceType);
	}
}

