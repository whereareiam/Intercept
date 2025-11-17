package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultMessageRegistryReloadTest {

	private Registry<Reloadable> reloadableRegistry;
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		reloadableRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(reloadableRegistry);
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(registry);
	}

	@Test
	void shouldClearAllEntriesOnReload() {
		// Register some entries
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "Message 1"));
		registry.register("key2", new DefaultMessageEntry(MessageType.MESSAGE, "Message 2"));
		registry.register("key3", new DefaultMessageEntry(MessageType.MESSAGE, "Message 3"));

		assertEquals(3, registry.getKeys().size());
		assertTrue(registry.exists("key1"));
		assertTrue(registry.exists("key2"));
		assertTrue(registry.exists("key3"));

		// Reload
		registry.reload();

		// All entries should be cleared
		assertEquals(0, registry.getKeys().size());
		assertFalse(registry.exists("key1"));
		assertFalse(registry.exists("key2"));
		assertFalse(registry.exists("key3"));
	}

	@Test
	void shouldAllowReregisteringAfterReload() {
		// Register initial entries
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "Original"));
		assertEquals("Original", registry.get("key1").getText());

		// Reload
		registry.reload();

		// Register new entries
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "New"));
		registry.register("key2", new DefaultMessageEntry(MessageType.MESSAGE, "Additional"));

		assertEquals("New", registry.get("key1").getText());
		assertEquals("Additional", registry.get("key2").getText());
		assertEquals(2, registry.getKeys().size());
	}

	@Test
	void shouldHandleReloadOnEmptyRegistry() {
		assertEquals(0, registry.getKeys().size());

		// Should not throw exception
		assertDoesNotThrow(() -> registry.reload());

		assertEquals(0, registry.getKeys().size());
	}

	@Test
	void shouldHandleMultipleConsecutiveReloads() {
		// Add entries
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "Message"));

		// Multiple reloads
		registry.reload();
		assertEquals(0, registry.getKeys().size());

		registry.reload();
		assertEquals(0, registry.getKeys().size());

		registry.reload();
		assertEquals(0, registry.getKeys().size());
	}

}

