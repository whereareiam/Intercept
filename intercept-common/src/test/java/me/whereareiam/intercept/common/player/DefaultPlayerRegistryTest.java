package me.whereareiam.intercept.common.player;

import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.player.PlayerAddedEvent;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Locale;
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

	private static final class TestInterceptPlayer extends InterceptPlayer {
		private TestInterceptPlayer(UUID uniqueId) {
			super(uniqueId, "Tester", Locale.ENGLISH);
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

