package me.whereareiam.intercept.common.player;

import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.player.PlayerAddedEvent;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.util.EventUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DefaultPlayerRegistryTest {
	private EventManager eventManager;
	private DefaultPlayerRegistry playerRegistry;

	@BeforeAll
	static void setUpEventUtil() {
		// Initialize EventUtil with a mock EventManager for tests
		EventManager mockEventManager = mock(EventManager.class);
		EventUtil.init(mockEventManager);
	}

	@BeforeEach
	void setUp() {
		eventManager = mock(EventManager.class);
		playerRegistry = new DefaultPlayerRegistry(eventManager);
	}

	@Test
	void syncPlayerDataPreservesInspectionModeAcrossWrappers() {
		UUID playerId = UUID.randomUUID();

		TestInterceptPlayer initialWrapper = new TestInterceptPlayer(playerId);
		initialWrapper.setInspectionMode(true);

		InterceptPlayer stored = playerRegistry.getPlayerData(playerId).orElseThrow();
		assertSame(initialWrapper, stored);
		assertTrue(stored.isInspectionMode());

		TestInterceptPlayer newWrapper = new TestInterceptPlayer(playerId);

		assertTrue(newWrapper.isInspectionMode());
		assertSame(newWrapper, playerRegistry.getPlayerData(playerId).orElseThrow());

		ArgumentCaptor<PlayerAddedEvent> captor = ArgumentCaptor.forClass(PlayerAddedEvent.class);
		verify(eventManager).call(captor.capture());
		assertEquals(initialWrapper, captor.getValue().getPlayer());
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	void syncPlayerDataPreservesLocaleAcrossWrappers() {
		UUID playerId = UUID.randomUUID();

		TestInterceptPlayer initialWrapper = new TestInterceptPlayer(playerId);
		initialWrapper.setLocale(Locale.FRANCE);

		TestInterceptPlayer newWrapper = new TestInterceptPlayer(playerId);

		assertEquals(Locale.FRANCE, newWrapper.getLocale());
		assertSame(newWrapper, playerRegistry.getPlayerData(playerId).orElseThrow());
	}

	@Test
	void getPlayerDataByUsernameIsCaseInsensitive() {
		UUID playerId = UUID.randomUUID();
		String username = "TestUser";

		new TestInterceptPlayer(playerId, username);

		Optional<InterceptPlayer> found = playerRegistry.getPlayerData("testuser");

		assertTrue(found.isPresent());
		assertEquals(username, found.orElseThrow().getUsername());
	}

	@Test
	void getPlayersReturnsAllEntries() {
		UUID first = UUID.randomUUID();
		UUID second = UUID.randomUUID();

		TestInterceptPlayer playerOne = new TestInterceptPlayer(first);
		TestInterceptPlayer playerTwo = new TestInterceptPlayer(second);

		assertTrue(playerRegistry.getPlayers().contains(playerOne));
		assertTrue(playerRegistry.getPlayers().contains(playerTwo));
	}

	private static final class TestInterceptPlayer extends InterceptPlayer {
		private TestInterceptPlayer(UUID uniqueId) {
			this(uniqueId, "Tester");
		}

		private TestInterceptPlayer(UUID uniqueId, String username) {
			super(uniqueId, username, Locale.ENGLISH);
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

