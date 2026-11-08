package me.whereareiam.intercept.adapter.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.dialectica.Dialectica;
import me.whereareiam.dialectica.adapter.jdbi.DialecticaPlugin;
import me.whereareiam.dialectica.adapter.jdbi.entity.EntityAccess;
import me.whereareiam.intercept.adapter.persistence.connection.DataSourceFactory;
import me.whereareiam.intercept.adapter.persistence.entity.RevisionEntity;
import me.whereareiam.intercept.adapter.persistence.schema.InterceptSchema;
import me.whereareiam.intercept.adapter.persistence.schema.SchemaInstaller;
import me.whereareiam.intercept.library.LibraryLoader;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.persistence.database.DatabaseSchema;
import me.whereareiam.intercept.persistence.database.DatabaseService;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Opens the database in the persistence phase, installs Intercept's schema and closes the pool when Intercept stops.
 */
@Singleton
public final class DefaultDatabaseService implements DatabaseService, Lifecycle {
	private final Provider<Persistence> persistence;
	private final Path dataPath;
	private final LibraryLoader libraries;
	private final Logger logger;

	private HikariDataSource dataSource;
	private Jdbi jdbi;
	private SchemaInstaller installer;

	@Inject
	public DefaultDatabaseService(
			@NotNull Provider<Persistence> persistence,
			@Named("dataPath") @NotNull Path dataPath,
			@NotNull LibraryLoader libraries,
			@Named("intercept") @NotNull Logger logger
	) {
		this.persistence = persistence;
		this.dataPath = dataPath;
		this.libraries = libraries;
		this.logger = logger;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.PERSISTENCE;
	}

	@Override
	public void start() {
		Persistence persistence = this.persistence.get();
		libraries.load(persistence.getType().name().toLowerCase(Locale.ROOT));

		dataSource = DataSourceFactory.create(persistence, dataPath);
		logger.info("Storing in " + persistence.getType() + " at " + DataSourceFactory.describe(persistence, dataPath));

		jdbi = Jdbi.create(dataSource);
		jdbi.installPlugin(new DialecticaPlugin());

		installer = new SchemaInstaller(jdbi, logger);
		installer.install(new InterceptSchema());
		access(RevisionEntity.class).insertIfAbsent(new RevisionEntity(RevisionEntity.SHARED, 0, System.currentTimeMillis()));
	}

	@Override
	public void stop() {
		if (dataSource != null) dataSource.close();

		dataSource = null;
		jdbi = null;
		installer = null;
	}

	@Override
	public @NotNull Jdbi jdbi() {
		Jdbi current = jdbi;
		if (current == null) throw new IllegalStateException("The database is not open");

		return current;
	}

	@Override
	public <T> @NotNull EntityAccess<T> access(@NotNull Class<T> type) {
		return EntityAccess.of(jdbi(), Dialectica.entity(type));
	}

	@Override
	public void install(@NotNull DatabaseSchema schema) {
		if (installer == null) throw new IllegalStateException("The database is not open");
		installer.install(schema);
	}
}
