package me.whereareiam.intercept.adapter.database.player;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.repository.player.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;

abstract class BasePlayerPersistenceIntegrationTest extends BaseTest {
	protected DefaultPlayerPersistenceService postgresService;
	protected DefaultPlayerPersistenceService mariaDbService;
	protected PlayerRepository postgresPlayerRepo;
	protected PlayerRepository mariaDbPlayerRepo;

	@BeforeEach
	void baseSetUp() {
		postgresPlayerRepo = getJdbi(DatabaseType.POSTGRES).onDemand(PlayerRepository.class);
		mariaDbPlayerRepo = getJdbi(DatabaseType.MARIADB).onDemand(PlayerRepository.class);

		postgresService = new DefaultPlayerPersistenceService(
				postgresPlayerRepo,
				getJdbi(DatabaseType.POSTGRES)
		);

		mariaDbService = new DefaultPlayerPersistenceService(
				mariaDbPlayerRepo,
				getJdbi(DatabaseType.MARIADB)
		);

		clearTables(DatabaseType.POSTGRES);
		clearTables(DatabaseType.MARIADB);
	}

	protected DefaultPlayerPersistenceService service(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresService : mariaDbService;
	}

	protected PlayerRepository playerRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresPlayerRepo : mariaDbPlayerRepo;
	}

	private void clearTables(DatabaseType type) {
		getJdbi(type).useHandle(handle -> handle.execute("DELETE FROM intercept_players"));
	}
}