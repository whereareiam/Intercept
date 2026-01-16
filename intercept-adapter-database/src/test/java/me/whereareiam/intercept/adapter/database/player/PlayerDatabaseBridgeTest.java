package me.whereareiam.intercept.adapter.database.player;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.database.PlayerPersistenceService;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.player.PlayerAddedEvent;
import me.whereareiam.intercept.event.player.PlayerRemovedEvent;
import me.whereareiam.intercept.event.player.change.PlayerInspectionModeChangedEvent;
import me.whereareiam.intercept.event.player.change.PlayerLocaleChangedEvent;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.model.player.PlayerData;
import me.whereareiam.intercept.util.EventUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PlayerDatabaseBridge event handling.
 * Tests the bridge between player lifecycle events and database persistence.
 */
@ExtendWith(MockitoExtension.class)
class PlayerDatabaseBridgeTest extends BasePlayerPersistenceIntegrationTest {
	@Mock(strictness = Mock.Strictness.LENIENT)
	private DatabaseService databaseService;
	
	@Mock
	private EventManager eventManager;

	@BeforeAll
	static void setUpEventUtil() {
		// Initialize EventUtil with a mock EventManager for tests
		EventManager mockEventManager = mock(EventManager.class);
		EventUtil.initialize(mockEventManager);
	}

	@BeforeEach
	void setUpMocks() {
		when(databaseService.isInitialized()).thenReturn(true);
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testOnPlayerAddedLoadsFromDatabase(DatabaseType type) {
		PlayerPersistenceService persistenceService = service(type);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		// Pre-save player data to database
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer existingPlayer = new TestInterceptPlayer(playerId, "ExistingPlayer", Locale.GERMANY);
		existingPlayer.setInspectionMode(true);
		existingPlayer.setLocale(Locale.GERMANY); // Explicitly set custom locale
		persistenceService.savePlayer(existingPlayer);

		// Create new player instance (simulating new login)
		TestInterceptPlayer newPlayer = new TestInterceptPlayer(playerId, "ExistingPlayer", Locale.ENGLISH);
		newPlayer.setInspectionMode(false);

		// Trigger event
		PlayerAddedEvent event = new PlayerAddedEvent(newPlayer);
		bridge.onPlayerAdded(event);

		// Verify data was loaded from database
		assertTrue(newPlayer.isInspectionMode());
		assertEquals(Locale.GERMANY, newPlayer.getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testOnPlayerAddedWithNonExistentPlayer(DatabaseType type) {
		PlayerPersistenceService persistenceService = service(type);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);
		when(databaseService.isInitialized()).thenReturn(true);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		// Create new player (not in database)
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer newPlayer = new TestInterceptPlayer(playerId, "NewPlayer", Locale.ENGLISH);
		newPlayer.setInspectionMode(false);

		// Trigger event
		PlayerAddedEvent event = new PlayerAddedEvent(newPlayer);
		bridge.onPlayerAdded(event);

		// Verify player values remain unchanged (database had no data)
		assertFalse(newPlayer.isInspectionMode());
		// Custom locale is null, so getLocale() returns client locale (Locale.ENGLISH from constructor)
		assertEquals(Locale.ENGLISH, newPlayer.getClientLocale());
		assertNull(newPlayer.getCustomLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testOnPlayerRemovedSavesToDatabase(DatabaseType type) {
		PlayerPersistenceService persistenceService = service(type);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);
		when(databaseService.isInitialized()).thenReturn(true);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		// Create and configure player
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "RemoveTest", Locale.FRANCE);
		player.setInspectionMode(true);
		player.setLocale(Locale.FRANCE); // Explicitly set custom locale

		// Trigger remove event
		PlayerRemovedEvent event = new PlayerRemovedEvent(player);
		bridge.onPlayerRemoved(event);

		// Verify data was saved
		Optional<PlayerData> saved = persistenceService.loadPlayer(playerId);
		assertTrue(saved.isPresent());
		assertTrue(saved.get().isInspectionMode());
		assertEquals(Locale.FRANCE, saved.get().getLocale());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testOnInspectionModeChangedSavesToDatabase(DatabaseType type) {
		PlayerPersistenceService persistenceService = service(type);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);
		when(databaseService.isInitialized()).thenReturn(true);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		// Create and save player first
		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "InspectionTest", Locale.ENGLISH);
		player.setInspectionMode(true);
		persistenceService.savePlayer(player); // Save initially

		// Trigger inspection mode changed event
		PlayerInspectionModeChangedEvent event = new PlayerInspectionModeChangedEvent(player, false, true);
		bridge.onInspectionModeChanged(event);

		// Verify event data
		assertFalse(event.isOldValue());
		assertTrue(event.isNewValue());

		// Verify data was saved
		Optional<PlayerData> saved = persistenceService.loadPlayer(playerId);
		assertTrue(saved.isPresent());
		assertTrue(saved.get().isInspectionMode());
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void testOnLocaleChangedSavesToDatabase(DatabaseType type) {
		PlayerPersistenceService persistenceService = service(type);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);
		when(databaseService.isInitialized()).thenReturn(true);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "LocaleTest", Locale.ENGLISH);
		player.setLocale(Locale.JAPAN); // Explicitly set custom locale

		// Trigger locale changed event
		PlayerLocaleChangedEvent event = new PlayerLocaleChangedEvent(player, Locale.ENGLISH, Locale.JAPAN);
		bridge.onLocaleChanged(event);

		// Verify event data
		assertEquals(Locale.ENGLISH, event.getOldLocale());
		assertEquals(Locale.JAPAN, event.getNewLocale());

		// Verify data was saved
		Optional<PlayerData> saved = persistenceService.loadPlayer(playerId);
		assertTrue(saved.isPresent());
		assertEquals(Locale.JAPAN, saved.get().getLocale());
	}

	@Test
	void testBridgeDoesNotOperateWhenDatabaseDisabled() {
		PlayerPersistenceService persistenceService = mock(PlayerPersistenceService.class);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);
		when(databaseService.isInitialized()).thenReturn(false);

		PlayerDatabaseBridge bridge = new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		UUID playerId = UUID.randomUUID();
		TestInterceptPlayer player = new TestInterceptPlayer(playerId, "DisabledTest", Locale.ENGLISH);

		// Trigger all events
		bridge.onPlayerAdded(new PlayerAddedEvent(player));
		bridge.onPlayerRemoved(new PlayerRemovedEvent(player));
		bridge.onInspectionModeChanged(new PlayerInspectionModeChangedEvent(player, false, true));
		bridge.onLocaleChanged(new PlayerLocaleChangedEvent(player, Locale.ENGLISH, Locale.GERMANY));

		// Verify persistence service was never called
		verifyNoInteractions(persistenceService);
	}

	@Test
	void testBridgeRegistersWithEventManager() {
		PlayerPersistenceService persistenceService = mock(PlayerPersistenceService.class);
		DatabaseService databaseService = mock(DatabaseService.class);
		EventManager eventManager = mock(EventManager.class);

		new PlayerDatabaseBridge(persistenceService, () -> databaseService, eventManager);

		verify(eventManager, times(1)).register(any(PlayerDatabaseBridge.class));
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