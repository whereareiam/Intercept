package me.whereareiam.intercept.adapter.persistence.schema;

import me.whereareiam.intercept.persistence.database.DatabaseSchema;
import me.whereareiam.strata.MigrationStream;
import me.whereareiam.strata.Strata;
import me.whereareiam.strata.adapter.database.DatabaseContext;
import me.whereareiam.strata.adapter.database.DatabaseTarget;
import me.whereareiam.strata.adapter.database.dialectica.SchemaCreation;
import me.whereareiam.strata.adapter.database.jdbi.JdbiConnector;
import me.whereareiam.strata.model.AppliedMigration;
import me.whereareiam.strata.model.MigrationReport;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

/**
 * Brings schemas up to date through Strata: a new database gets the latest tables, an existing one its missing
 * migrations.
 */
public final class SchemaInstaller {
	private final Jdbi jdbi;
	private final Logger logger;

	public SchemaInstaller(@NotNull Jdbi jdbi, @NotNull Logger logger) {
		this.jdbi = jdbi;
		this.logger = logger;
	}

	/**
	 * Installs one schema.
	 *
	 * @param schema the schema
	 */
	public synchronized void install(@NotNull DatabaseSchema schema) {
		MigrationStream<DatabaseContext> stream = MigrationStream.<DatabaseContext>builder()
				.id(schema.stream())
				.target(new DatabaseTarget(new JdbiConnector(jdbi)))
				.creation(SchemaCreation.of(schema.schema()))
				.migrations(schema.migrations())
				.build();

		MigrationReport report = new Strata(List.of(stream)).migrate();
		for (AppliedMigration applied : report.getApplied().getOrDefault(schema.stream(), List.of()))
			logger.info("Database schema " + schema.stream() + ": applied " + applied.getVersion() + " " + applied.getName());
	}
}
