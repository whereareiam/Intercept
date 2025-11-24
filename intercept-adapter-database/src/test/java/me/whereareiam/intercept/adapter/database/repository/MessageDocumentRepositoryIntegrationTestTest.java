package me.whereareiam.intercept.adapter.database.repository;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class MessageDocumentRepositoryIntegrationTestTest extends BaseTest {
	private MessageFileRepository postgresRepo;
	private MessageFileRepository mariaDbRepo;

	@BeforeEach
	void setUp() {
		postgresRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageFileRepository.class);

		// Clear tables
		getJdbi(DatabaseType.POSTGRES).useHandle(handle ->
				handle.execute("DELETE FROM intercept_message_files"));
		getJdbi(DatabaseType.MARIADB).useHandle(handle ->
				handle.execute("DELETE FROM intercept_message_files"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testInsertAndFindById(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("errors/permissions");

		MessageFileEntity saved = repo.save(entity);
		assertNotNull(saved.getId());
		assertEquals("errors/permissions", saved.getFilePath());

		MessageFileEntity found = repo.findById(saved.getId()).orElse(null);
		assertNotNull(found);
		assertEquals(saved.getId(), found.getId());
		assertEquals("errors/permissions", found.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testFindByFilePath(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("common/colors");
		repo.save(entity);

		MessageFileEntity found = repo.findByFilePath("common/colors").orElse(null);
		assertNotNull(found);
		assertEquals("common/colors", found.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUpdate(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test");
		MessageFileEntity saved = repo.save(entity);

		saved.setFilePath("test_updated");
		repo.save(saved);

		MessageFileEntity updated = repo.findById(saved.getId()).orElse(null);
		assertNotNull(updated);
		assertEquals("test_updated", updated.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDeleteById(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test");
		MessageFileEntity saved = repo.save(entity);

		repo.deleteById(saved.getId());

		assertFalse(repo.existsById(saved.getId()));
		assertTrue(repo.findById(saved.getId()).isEmpty());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testExistsById(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test");
		MessageFileEntity saved = repo.save(entity);

		assertTrue(repo.existsById(saved.getId()));
		assertFalse(repo.existsById(999L));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testExistsByFilePath(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test");
		repo.save(entity);

		assertTrue(repo.existsByFilePath("test"));
		assertFalse(repo.existsByFilePath("nonexistent"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testCount(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		assertEquals(0, repo.count());

		MessageFileEntity entity1 = new MessageFileEntity();
		entity1.setFilePath("test1");
		repo.save(entity1);

		MessageFileEntity entity2 = new MessageFileEntity();
		entity2.setFilePath("test2");
		repo.save(entity2);

		assertEquals(2, repo.count());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testFindAll(DatabaseType type) {
		MessageFileRepository repo = type == DatabaseType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity1 = new MessageFileEntity();
		entity1.setFilePath("test1");
		repo.save(entity1);

		MessageFileEntity entity2 = new MessageFileEntity();
		entity2.setFilePath("test2");
		repo.save(entity2);

		var all = repo.findAll();
		assertEquals(2, all.size());
	}
}