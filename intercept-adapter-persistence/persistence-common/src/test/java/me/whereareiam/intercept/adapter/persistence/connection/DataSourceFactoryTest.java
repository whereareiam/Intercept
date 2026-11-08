package me.whereareiam.intercept.adapter.persistence.connection;

import me.whereareiam.intercept.persistence.database.DatabaseType;
import me.whereareiam.intercept.persistence.database.Persistence;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSourceFactoryTest {
	@Test
	void eachDatabaseGetsItsUrl() {
		Persistence persistence = new Persistence();
		persistence.setHost("db");
		persistence.setPort(5432);

		persistence.setType(DatabaseType.POSTGRES);
		assertEquals("jdbc:postgresql://db:5432/intercept", DataSourceFactory.url(persistence, Path.of("/srv")));

		persistence.setType(DatabaseType.MYSQL);
		assertEquals("jdbc:mariadb://db:5432/intercept", DataSourceFactory.url(persistence, Path.of("/srv")));

		persistence.setType(DatabaseType.SQLITE);
		assertEquals("jdbc:sqlite:/srv/intercept.db", DataSourceFactory.url(persistence, Path.of("/srv")));
	}
}
