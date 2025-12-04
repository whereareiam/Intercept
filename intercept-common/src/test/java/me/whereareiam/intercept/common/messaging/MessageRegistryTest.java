package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageRegistryTest {
	private Registry<Reloadable> reloadableRegistry;
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		reloadableRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(reloadableRegistry);
	}

	@Test
	void shouldRegisterAndGetEntry() {
		CompiledMessageEntry entry = new CompiledMessageEntry(MessageType.MESSAGE, "Test");
		registry.register("test.key", entry);

		CompiledMessageEntry retrieved = registry.get("test.key");
		assertNotNull(retrieved);
		assertEquals("Test", retrieved.getText());
	}

	@Test
	void shouldReturnNullForNonExistentKey() {
		CompiledMessageEntry entry = registry.get("nonexistent");
		assertNull(entry);
	}

	@Test
	void shouldCheckIfKeyExists() {
		CompiledMessageEntry entry = new CompiledMessageEntry(MessageType.MESSAGE, "Test");
		registry.register("test.key", entry);

		assertTrue(registry.exists("test.key"));
		assertFalse(registry.exists("other.key"));
	}

	@Test
	void shouldGetAllKeys() {
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("key2", new CompiledMessageEntry(MessageType.MESSAGE, "2"));
		registry.register("key3", new CompiledMessageEntry(MessageType.MESSAGE, "3"));

		Set<String> keys = registry.getKeys();
		assertEquals(3, keys.size());
		assertTrue(keys.contains("key1"));
		assertTrue(keys.contains("key2"));
		assertTrue(keys.contains("key3"));
	}

	@Test
	void shouldGetKeysByPrefix() {
		registry.register("errors.permission1", new CompiledMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("errors.permission2", new CompiledMessageEntry(MessageType.MESSAGE, "2"));
		registry.register("errors.database.connection", new CompiledMessageEntry(MessageType.MESSAGE, "3"));
		registry.register("success.action", new CompiledMessageEntry(MessageType.MESSAGE, "4"));

		Set<String> errorKeys = registry.getKeysByPrefix("errors");
		assertEquals(3, errorKeys.size());

		Set<String> permissionKeys = registry.getKeysByPrefix("errors.permission");
		assertEquals(2, permissionKeys.size());

		Set<String> databaseKeys = registry.getKeysByPrefix("errors.database");
		assertEquals(1, databaseKeys.size());
	}

	@Test
	void shouldGetAllEntries() {
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("key2", new CompiledMessageEntry(MessageType.TEMPLATE, "2"));

		Map<String, CompiledMessageEntry> entries = registry.getAllEntries();
		assertEquals(2, entries.size());
		assertNotNull(entries.get("key1"));
		assertNotNull(entries.get("key2"));
	}

	@Test
	void shouldHandleEmptyRegistry() {
		assertTrue(registry.getKeys().isEmpty());
		assertTrue(registry.getAllEntries().isEmpty());
		assertFalse(registry.exists("any"));
	}

	@Test
	void shouldOverwriteExistingKey() {
		registry.register("key", new CompiledMessageEntry(MessageType.MESSAGE, "First"));
		registry.register("key", new CompiledMessageEntry(MessageType.MESSAGE, "Second"));

		CompiledMessageEntry entry = registry.get("key");
		assertEquals("Second", entry.getText());
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(registry);
	}

	@Test
	void shouldClearAllEntriesOnReload() {
		// Register some entries
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "Message 1"));
		registry.register("key2", new CompiledMessageEntry(MessageType.MESSAGE, "Message 2"));
		registry.register("key3", new CompiledMessageEntry(MessageType.MESSAGE, "Message 3"));

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
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "Original"));
		assertEquals("Original", registry.get("key1").getText());

		// Reload
		registry.reload();

		// Register new entries
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "New"));
		registry.register("key2", new CompiledMessageEntry(MessageType.MESSAGE, "Additional"));

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
		registry.register("key1", new CompiledMessageEntry(MessageType.MESSAGE, "Message"));

		// Multiple reloads
		registry.reload();
		assertEquals(0, registry.getKeys().size());

		registry.reload();
		assertEquals(0, registry.getKeys().size());

		registry.reload();
		assertEquals(0, registry.getKeys().size());
	}
}