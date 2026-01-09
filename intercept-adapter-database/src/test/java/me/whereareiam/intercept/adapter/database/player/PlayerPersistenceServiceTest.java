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
		player.setLocale(Locale.US); // Explicitly set custom locale

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
		player.setLocale(Locale.GERMANY); // Explicitly set custom locale

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
		player1.setLocale(Locale.US); // Explicitly set custom locale
		service.savePlayer(player1);

		// Update data
		TestInterceptPlayer player2 = new TestInterceptPlayer(playerId, "Player", Locale.FRANCE);
		player2.setInspectionMode(true);
		player2.setLocale(Locale.FRANCE); // Explicitly set custom locale
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
		player.setLocale(Locale.JAPAN); // Explicitly set custom locale
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
	void testDeletePlayer(DatabaseType type) {
		DefaultPlayerPersistenceService service = service(type);
		UUID playerId = UUID.randomUUID();
		
		// Save player
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "DeleteTest", Locale.UK);
		player.setLocale(Locale.UK); // Explicitly set custom locale
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





	/**
	 * Test player implementation for integration tests.
	 */
	private static final class TestInterceptPlayer extends InterceptPlayer {
		private final Locale clientLocale;

		private TestInterceptPlayer(UUID uniqueId, String username, Locale clientLocale) {
			super(uniqueId, username);
			this.clientLocale = clientLocale;
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
		public Locale getClientLocale() {
			return clientLocale;
		}

		@Override
		@NotNull
		public Audience getAudience() {
			return Audience.empty();
		}
	}
}
