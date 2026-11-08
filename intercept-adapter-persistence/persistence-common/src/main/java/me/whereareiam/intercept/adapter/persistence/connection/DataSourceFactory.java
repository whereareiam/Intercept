package me.whereareiam.intercept.adapter.persistence.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.intercept.persistence.database.DatabaseType;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Builds the connection pool for the configured database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceFactory {
	/**
	 * Builds a pool. It connects lazily, so a database that is down when the server starts is retried.
	 *
	 * @param persistence the configuration
	 * @param dataPath    Intercept's folder, which file databases are relative to
	 * @return the pool
	 */
	public static @NotNull HikariDataSource create(@NotNull Persistence persistence, @NotNull Path dataPath) {
		HikariConfig config = new HikariConfig();
		config.setPoolName("Intercept");
		config.setJdbcUrl(url(persistence, dataPath));
		config.setDriverClassName(driver(persistence.getType()));
		if (isServer(persistence.getType())) {
			config.setUsername(persistence.getUsername());
			config.setPassword(persistence.getPassword());
		}

		Persistence.Pool pool = persistence.getPool();
		config.setMaximumPoolSize(pool.getMaximumSize());
		config.setMinimumIdle(pool.getMinimumIdle());
		config.setConnectionTimeout(pool.getConnectionTimeout());
		config.setInitializationFailTimeout(-1);

		return new HikariDataSource(config);
	}

	/**
	 * Describes where the database is, without credentials.
	 *
	 * @param persistence the configuration
	 * @param dataPath    Intercept's folder
	 * @return a description for the log
	 */
	public static @NotNull String describe(@NotNull Persistence persistence, @NotNull Path dataPath) {
		return isServer(persistence.getType())
				? persistence.getHost() + ":" + persistence.getPort() + "/" + persistence.getDatabase()
				: dataPath.resolve(persistence.getFile()).toString();
	}

	static @NotNull String url(@NotNull Persistence persistence, @NotNull Path dataPath) {
		String server = persistence.getHost() + ":" + persistence.getPort() + "/" + persistence.getDatabase();
		Path file = dataPath.resolve(persistence.getFile()).toAbsolutePath();

		return switch (persistence.getType()) {
			case SQLITE -> "jdbc:sqlite:" + file;
			case H2 -> "jdbc:h2:file:" + file;
			case MYSQL -> "jdbc:mariadb://" + server;
			case POSTGRES -> "jdbc:postgresql://" + server;
		};
	}

	private static String driver(DatabaseType type) {
		return switch (type) {
			case SQLITE -> "org.sqlite.JDBC";
			case H2 -> "org.h2.Driver";
			case MYSQL -> "org.mariadb.jdbc.Driver";
			case POSTGRES -> "org.postgresql.Driver";
		};
	}

	private static boolean isServer(DatabaseType type) {
		return type == DatabaseType.MYSQL || type == DatabaseType.POSTGRES;
	}
}
