package me.whereareiam.intercept.adapter.database.dialect;

import me.whereareiam.intercept.adapter.database.config.DialectConfig;
import me.whereareiam.intercept.type.PersistenceType;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjects;

/**
 * Jdbi plugin that enables database-specific SQL resolution.
 * <p>
 * This plugin:
 * 1. Stores the {@link PersistenceType} in Jdbi's configuration
 * 2. Configures a custom {@link DialectLocator} that resolves SQL from {@link StatementProvider}
 * <p>
 * Install this plugin when creating your Jdbi instance:
 * <pre>{@code
 * Jdbi jdbi = Jdbi.create(dataSource);
 * jdbi.installPlugin(new DialectPlugin(persistence.getType()));
 * }</pre>
 */
public final class DialectPlugin implements JdbiPlugin {
	private final PersistenceType persistenceType;

	/**
	 * Creates a new DialectPlugin with the given PersistenceType.
	 *
	 * @param persistenceType the database type (POSTGRES or MARIADB)
	 */
	public DialectPlugin(PersistenceType persistenceType) {
		if (persistenceType == null) {
			throw new IllegalArgumentException("PersistenceType must not be null");
		}
		this.persistenceType = persistenceType;
	}

	@Override
	public void customizeJdbi(Jdbi jdbi) {
		// Store PersistenceType in Jdbi config
		DialectConfig config = jdbi.getConfig().get(DialectConfig.class);
		config.setPersistenceType(persistenceType);

		// Configure custom SqlLocator that resolves from StatementProvider
		jdbi.getConfig(SqlObjects.class).setSqlLocator(new DialectLocator());
	}
}

