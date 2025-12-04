package me.whereareiam.intercept.adapter.database.player;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;
import me.whereareiam.intercept.util.EventUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for PlayerPersistenceService.
 * Tests save, load, and delete operations across PostgreSQL and MariaDB.
 */
class PlayerPersistenceServiceTest extends BasePlayerPersistenceIntegrationTest {
	@BeforeAll
	static void setUpEventUtil() {
		// Initialize EventUtil with a mock EventManager for tests
		EventManager mockEventManager = mock(EventManager.class);
		EventUtil.initialize(mockEventManager);
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testSaveNewPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "TestPlayer", Locale.US);
		player.setInspectionMode(true);

		service.savePlayer(player);

		Optional<PlayerEntity> entity = playerRepo(type).findByUniqueId(playerId);
		assertTrue(entity.isPresent());
		assertEquals(playerId, entity.get().getUniqueId());
		assertTrue(entity.get().isInspectionMode());
		assertEquals(Locale.US, entity.get().getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testSavePlayerWithDifferentLocale(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "GermanPlayer", Locale.GERMANY);
		player.setInspectionMode(false);

		service.savePlayer(player);

		Optional<PlayerEntity> entity = playerRepo(type).findByUniqueId(playerId);
		assertTrue(entity.isPresent());
		assertEquals(Locale.GERMANY, entity.get().getLocale());
		assertFalse(entity.get().isInspectionMode());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testUpdateExistingPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		// Save initial data
		TestInterceptPlayer player1 = new TestInterceptPlayer(playerId, "Player", Locale.US);
		player1.setInspectionMode(false);
		service.savePlayer(player1);

		// Update data
		TestInterceptPlayer player2 = new TestInterceptPlayer(playerId, "Player", Locale.FRANCE);
		player2.setInspectionMode(true);
		service.savePlayer(player2);

		// Verify update
		Optional<PlayerEntity> entity = playerRepo(type).findByUniqueId(playerId);
		assertTrue(entity.isPresent());
		assertTrue(entity.get().isInspectionMode());
		assertEquals(Locale.FRANCE, entity.get().getLocale());
		
		// Verify only one record exists
		assertEquals(1, playerRepo(type).findByUniqueId(playerId).stream().count());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testLoadExistingPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		// Save player first
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "LoadTest", Locale.JAPAN);
		player.setInspectionMode(true);
		service.savePlayer(player);

		// Load player
		Optional<PlayerData> loaded = service.loadPlayer(playerId);
		
		assertTrue(loaded.isPresent());
		assertEquals(playerId, loaded.get().getUniqueId());
		assertTrue(loaded.get().isInspectionMode());
		assertEquals(Locale.JAPAN, loaded.get().getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testLoadNonExistentPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID nonExistentId = UUID.randomUUID();

		Optional<PlayerData> loaded = service.loadPlayer(nonExistentId);
		
		assertFalse(loaded.isPresent());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testLoadPlayerWithNullUUID(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);

		Optional<PlayerData> loaded = service.loadPlayer(null);
		
		assertFalse(loaded.isPresent());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDeletePlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		// Save player
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "DeleteTest", Locale.UK);
		service.savePlayer(player);
		assertTrue(playerRepo(type).exists(playerId));

		// Delete player
		service.deletePlayer(playerId);
		
		assertFalse(playerRepo(type).exists(playerId));
		assertFalse(service.loadPlayer(playerId).isPresent());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDeleteNonExistentPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID nonExistentId = UUID.randomUUID();

		assertDoesNotThrow(() -> service.deletePlayer(nonExistentId));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testDeletePlayerWithNullUUID(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);

		assertDoesNotThrow(() -> service.deletePlayer(null));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testSavePlayerWithNullPlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);

		assertDoesNotThrow(() -> service.savePlayer(null));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testSaveMultiplePlayers(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		
		UUID player1Id = UUID.randomUUID();
		UUID player2Id = UUID.randomUUID();
		UUID player3Id = UUID.randomUUID();

		TestInterceptPlayer player1 = new TestInterceptPlayer(player1Id, "Player1", Locale.US);
		player1.setInspectionMode(true);
		
		TestInterceptPlayer player2 = new TestInterceptPlayer(player2Id, "Player2", Locale.GERMANY);
		player2.setInspectionMode(false);
		
		TestInterceptPlayer player3 = new TestInterceptPlayer(player3Id, "Player3", Locale.FRANCE);
		player3.setInspectionMode(true);

		service.savePlayer(player1);
		service.savePlayer(player2);
		service.savePlayer(player3);

		assertTrue(playerRepo(type).exists(player1Id));
		assertTrue(playerRepo(type).exists(player2Id));
		assertTrue(playerRepo(type).exists(player3Id));
		
		Optional<PlayerData> loaded1 = service.loadPlayer(player1Id);
		Optional<PlayerData> loaded2 = service.loadPlayer(player2Id);
		Optional<PlayerData> loaded3 = service.loadPlayer(player3Id);
		
		assertTrue(loaded1.isPresent() && loaded1.get().isInspectionMode());
		assertTrue(loaded2.isPresent() && !loaded2.get().isInspectionMode());
		assertTrue(loaded3.isPresent() && loaded3.get().isInspectionMode());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testSaveAndLoadCycle(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		// Initial save
		TestInterceptPlayer player1 = new TestInterceptPlayer(playerId, "CycleTest", Locale.CANADA);
		player1.setInspectionMode(false);
		service.savePlayer(player1);

		// Load and verify
		Optional<PlayerData> loaded1 = service.loadPlayer(playerId);
		assertTrue(loaded1.isPresent());
		assertFalse(loaded1.get().isInspectionMode());
		assertEquals(Locale.CANADA, loaded1.get().getLocale());

		// Update
		TestInterceptPlayer player2 = new TestInterceptPlayer(playerId, "CycleTest", Locale.ITALY);
		player2.setInspectionMode(true);
		service.savePlayer(player2);

		// Load and verify update
		Optional<PlayerData> loaded2 = service.loadPlayer(playerId);
		assertTrue(loaded2.isPresent());
		assertTrue(loaded2.get().isInspectionMode());
		assertEquals(Locale.ITALY, loaded2.get().getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testInspectionModeToggle(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "ToggleTest", Locale.US);
		
		// Save with inspection disabled
		player.setInspectionMode(false);
		service.savePlayer(player);
		assertFalse(service.loadPlayer(playerId).get().isInspectionMode());

		// Toggle to enabled
		player.setInspectionMode(true);
		service.savePlayer(player);
		assertTrue(service.loadPlayer(playerId).get().isInspectionMode());

		// Toggle back to disabled
		player.setInspectionMode(false);
		service.savePlayer(player);
		assertFalse(service.loadPlayer(playerId).get().isInspectionMode());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testLocaleChange(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "LocaleTest", Locale.US);
		service.savePlayer(player);
		assertEquals(Locale.US, service.loadPlayer(playerId).get().getLocale());

		player.setLocale(Locale.GERMANY);
		service.savePlayer(player);
		assertEquals(Locale.GERMANY, service.loadPlayer(playerId).get().getLocale());

		player.setLocale(Locale.JAPAN);
		service.savePlayer(player);
		assertEquals(Locale.JAPAN, service.loadPlayer(playerId).get().getLocale());
	}

	/**
	 * Test player implementation for integration tests.
	 */
	private static final class TestInterceptPlayer extends InterceptPlayer {
		private TestInterceptPlayer(UUID uniqueId, String username, Locale locale) {
			super(uniqueId, username, locale);
		}

		@Override
		public void sendMessage(@NotNull Component message) {
			// no-op for tests
		}

		@Override
		public boolean hasPermission(@NotNull String permission) {
			return true;
		}

		@Override
		@NotNull
		public Audience getAudience() {
			return Audience.empty();
		}
	}
}
