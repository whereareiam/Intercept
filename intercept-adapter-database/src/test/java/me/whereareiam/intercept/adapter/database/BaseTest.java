package me.whereareiam.intercept.adapter.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.intercept.adapter.database.converter.LocaleArgumentFactory;
import me.whereareiam.intercept.adapter.database.converter.LocaleColumnMapper;
import me.whereareiam.intercept.adapter.database.dialect.DialectPlugin;
import me.whereareiam.intercept.adapter.database.schema.SchemaInitializer;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.type.PersistenceType;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Base class for database integration tests using Testcontainers.
 * Provides PostgreSQL and MariaDB containers and Jdbi instances.
 */
@Testcontainers
public abstract class BaseTest {
	@Container
	protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:18.1");

	@Container
	protected static final MariaDBContainer<?> mariaDbContainer = new MariaDBContainer<>("mariadb:12");

	protected static Jdbi postgresJdbi;
	protected static Jdbi mariaDbJdbi;
	private static final List<HikariDataSource> dataSources = new ArrayList<>();

	@BeforeAll
	static void setUpContainers() {
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);

		postgresJdbi = createJdbi(
				postgresContainer.getJdbcUrl(),
				postgresContainer.getUsername(),
				postgresContainer.getPassword()
		);
		mariaDbJdbi = createJdbi(
				mariaDbContainer.getJdbcUrl(),
				mariaDbContainer.getUsername(),
				mariaDbContainer.getPassword()
		);

		// Initialize schema
		SchemaInitializer.createTables(postgresJdbi, PersistenceType.POSTGRES);
		SchemaInitializer.createTables(mariaDbJdbi, PersistenceType.MARIADB);
	}

	@AfterAll
	static void tearDownContainers() {
		for (HikariDataSource dataSource : dataSources) {
			if (dataSource != null && !dataSource.isClosed()) {
				dataSource.close();
			}
		}
		dataSources.clear();
	}

	private static Jdbi createJdbi(String jdbcUrl, String username, String password) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setMaximumPoolSize(5);
		config.setMinimumIdle(1);

		HikariDataSource dataSource = new HikariDataSource(config);
		dataSources.add(dataSource);
		Jdbi jdbi = Jdbi.create(dataSource);
		jdbi.installPlugin(new SqlObjectPlugin());
		
		// Determine PersistenceType from JDBC URL
		PersistenceType persistenceType = jdbcUrl.contains("postgresql") ? PersistenceType.POSTGRES : PersistenceType.MARIADB;
		jdbi.installPlugin(new DialectPlugin(persistenceType));

		// Register Locale converters
		jdbi.registerColumnMapper(new LocaleColumnMapper());
		jdbi.getConfig().get(Arguments.class).register(new LocaleArgumentFactory());

		return jdbi;
	}

	protected Jdbi getJdbi(PersistenceType type) {
		return switch (type) {
			case POSTGRES -> postgresJdbi;
			case MARIADB -> mariaDbJdbi;
		};
	}
}