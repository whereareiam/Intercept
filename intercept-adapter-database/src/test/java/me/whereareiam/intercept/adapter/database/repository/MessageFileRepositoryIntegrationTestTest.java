package me.whereareiam.intercept.adapter.database.repository;

import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.type.PersistenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class MessageFileRepositoryIntegrationTestTest extends BaseTest {
	private MessageFileRepository postgresRepo;
	private MessageFileRepository mariaDbRepo;

	@BeforeEach
	void setUp() {
		postgresRepo = getJdbi(PersistenceType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbRepo = getJdbi(PersistenceType.MARIADB).onDemand(MessageFileRepository.class);

		// Clear tables
		getJdbi(PersistenceType.POSTGRES).useHandle(handle ->
				handle.execute("DELETE FROM intercept_message_files"));
		getJdbi(PersistenceType.MARIADB).useHandle(handle ->
				handle.execute("DELETE FROM intercept_message_files"));
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testInsertAndFindById(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("errors/permissions.yml");

		MessageFileEntity saved = repo.save(entity);
		assertNotNull(saved.getId());
		assertEquals("errors/permissions.yml", saved.getFilePath());

		MessageFileEntity found = repo.findById(saved.getId()).orElse(null);
		assertNotNull(found);
		assertEquals(saved.getId(), found.getId());
		assertEquals("errors/permissions.yml", found.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testFindByFilePath(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("common/colors.yml");
		repo.save(entity);

		MessageFileEntity found = repo.findByFilePath("common/colors.yml").orElse(null);
		assertNotNull(found);
		assertEquals("common/colors.yml", found.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testUpdate(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test.yml");
		MessageFileEntity saved = repo.save(entity);

		saved.setFilePath("test_updated.yml");
		repo.save(saved);

		MessageFileEntity updated = repo.findById(saved.getId()).orElse(null);
		assertNotNull(updated);
		assertEquals("test_updated.yml", updated.getFilePath());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testDeleteById(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test.yml");
		MessageFileEntity saved = repo.save(entity);

		repo.deleteById(saved.getId());

		assertFalse(repo.existsById(saved.getId()));
		assertTrue(repo.findById(saved.getId()).isEmpty());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testExistsById(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test.yml");
		MessageFileEntity saved = repo.save(entity);

		assertTrue(repo.existsById(saved.getId()));
		assertFalse(repo.existsById(999L));
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testExistsByFilePath(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity = new MessageFileEntity();
		entity.setFilePath("test.yml");
		repo.save(entity);

		assertTrue(repo.existsByFilePath("test.yml"));
		assertFalse(repo.existsByFilePath("nonexistent.yml"));
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testCount(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		assertEquals(0, repo.count());

		MessageFileEntity entity1 = new MessageFileEntity();
		entity1.setFilePath("test1.yml");
		repo.save(entity1);

		MessageFileEntity entity2 = new MessageFileEntity();
		entity2.setFilePath("test2.yml");
		repo.save(entity2);

		assertEquals(2, repo.count());
	}

	@ParameterizedTest
	@EnumSource(PersistenceType.class)
	void testFindAll(PersistenceType type) {
		MessageFileRepository repo = type == PersistenceType.POSTGRES ? postgresRepo : mariaDbRepo;

		MessageFileEntity entity1 = new MessageFileEntity();
		entity1.setFilePath("test1.yml");
		repo.save(entity1);

		MessageFileEntity entity2 = new MessageFileEntity();
		entity2.setFilePath("test2.yml");
		repo.save(entity2);

		var all = repo.findAll();
		assertEquals(2, all.size());
	}
}