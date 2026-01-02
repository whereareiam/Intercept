package me.whereareiam.intercept.adapter.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.dialectica.Dialectica;
import me.whereareiam.dialectica.DialectPlugin;
import me.whereareiam.dialectica.SchemaManager;
import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.converter.LocaleArgumentFactory;
import me.whereareiam.intercept.adapter.database.converter.LocaleColumnMapper;
import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import me.whereareiam.intercept.adapter.database.entity.message.*;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Base class for database integration tests using Testcontainers.
 * Provides PostgreSQL and MariaDB containers and Jdbi instances.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseTest {
	@Container
	static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:18.1")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	@Container
	static MariaDBContainer mariaDbContainer = new MariaDBContainer("mariadb:12")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

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

		// Initialize schema using Dialectica
		SchemaManager postgresSchemaManager = Dialectica.schema(postgresJdbi)
				.registerEntity(PlayerEntity.class)
				.registerEntity(MessageFileEntity.class)
				.registerEntity(MessageEntryEntity.class)
				.registerEntity(MessageTranslationEntity.class)
				.registerEntity(MessageRegexPatternEntity.class)
				.registerEntity(MessageRegexPlaceholderEntity.class);
		postgresSchemaManager.initialize();

		SchemaManager mariaDbSchemaManager = Dialectica.schema(mariaDbJdbi)
				.registerEntity(PlayerEntity.class)
				.registerEntity(MessageFileEntity.class)
				.registerEntity(MessageEntryEntity.class)
				.registerEntity(MessageTranslationEntity.class)
				.registerEntity(MessageRegexPatternEntity.class)
				.registerEntity(MessageRegexPlaceholderEntity.class);
		mariaDbSchemaManager.initialize();
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

		// Determine DatabaseType from JDBC URL
		DatabaseType databaseType = jdbcUrl.contains("postgresql") ? DatabaseType.POSTGRES : DatabaseType.MARIADB;
		jdbi.installPlugin(new DialectPlugin(databaseType));

		// Register Locale converters
		jdbi.registerColumnMapper(new LocaleColumnMapper());
		jdbi.getConfig().get(Arguments.class).register(new LocaleArgumentFactory());

		return jdbi;
	}

	protected Jdbi getJdbi(DatabaseType type) {
		return switch (type) {
			case POSTGRES -> postgresJdbi;
			case MARIADB -> mariaDbJdbi;
		};
	}
}