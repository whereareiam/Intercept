package me.whereareiam.intercept.adapter.persistence;

import me.whereareiam.intercept.persistence.database.DatabaseType;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The databases store tests run on: H2 and SQLite always, and PostgreSQL in a container when
 * {@code intercept.test.databaseServers} is set, as {@code integrationTest} does.
 */
public final class TestDatabases {
	private static PostgreSQLContainer postgres;

	private TestDatabases() {
	}

	/**
	 * Lists the databases to run on.
	 *
	 * @return their types
	 */
	public static Stream<DatabaseType> types() {
		List<DatabaseType> types = new ArrayList<>(List.of(DatabaseType.H2, DatabaseType.SQLITE));
		if (Boolean.getBoolean("intercept.test.databaseServers")) types.add(DatabaseType.POSTGRES);

		return types.stream();
	}

	/**
	 * Opens an empty database of a type, with Intercept's schema installed.
	 *
	 * @param type   the database
	 * @param folder a fresh folder for file databases
	 * @return the open database; stop it after the test
	 */
	public static DefaultDatabaseService open(DatabaseType type, Path folder) {
		Persistence persistence = new Persistence();
		persistence.setType(type);
		if (type == DatabaseType.POSTGRES) {
			PostgreSQLContainer server = postgres();
			persistence.setHost(server.getHost());
			persistence.setPort(server.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
			persistence.setDatabase(server.getDatabaseName());
			persistence.setUsername(server.getUsername());
			persistence.setPassword(server.getPassword());
		}

		DefaultDatabaseService database = new DefaultDatabaseService(() -> persistence, folder, groups -> {
		}, Logger.getAnonymousLogger());
		database.start();
		if (type == DatabaseType.POSTGRES) clear(database);

		return database;
	}

	private static synchronized PostgreSQLContainer postgres() {
		if (postgres == null) {
			postgres = new PostgreSQLContainer("postgres:17-alpine");
			postgres.start();
		}

		return postgres;
	}

	// The server outlives a test, so each test starts from empty tables.
	private static void clear(DefaultDatabaseService database) {
		database.jdbi().useHandle(handle -> {
			handle.execute("DELETE FROM intercept_shared_message");
			handle.execute("DELETE FROM intercept_shared_rule");
			handle.execute("DELETE FROM intercept_language");
			handle.execute("UPDATE intercept_revision SET revision = 0");
		});
	}
}
