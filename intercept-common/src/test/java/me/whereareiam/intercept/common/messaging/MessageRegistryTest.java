package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MessageRegistryTest {
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		Registry<Reloadable> mockRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(mockRegistry);
	}

	@Test
	void shouldRegisterAndGetEntry() {
		MessageEntry entry = new DefaultMessageEntry(MessageType.MESSAGE, "Test");
		registry.register("test.key", entry);

		MessageEntry retrieved = registry.get("test.key");
		assertNotNull(retrieved);
		assertEquals("Test", retrieved.getText());
	}

	@Test
	void shouldReturnNullForNonExistentKey() {
		MessageEntry entry = registry.get("nonexistent");
		assertNull(entry);
	}

	@Test
	void shouldCheckIfKeyExists() {
		MessageEntry entry = new DefaultMessageEntry(MessageType.MESSAGE, "Test");
		registry.register("test.key", entry);

		assertTrue(registry.exists("test.key"));
		assertFalse(registry.exists("other.key"));
	}

	@Test
	void shouldGetAllKeys() {
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("key2", new DefaultMessageEntry(MessageType.MESSAGE, "2"));
		registry.register("key3", new DefaultMessageEntry(MessageType.MESSAGE, "3"));

		Set<String> keys = registry.getKeys();
		assertEquals(3, keys.size());
		assertTrue(keys.contains("key1"));
		assertTrue(keys.contains("key2"));
		assertTrue(keys.contains("key3"));
	}

	@Test
	void shouldGetKeysByPrefix() {
		registry.register("errors.permission1", new DefaultMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("errors.permission2", new DefaultMessageEntry(MessageType.MESSAGE, "2"));
		registry.register("errors.database.connection", new DefaultMessageEntry(MessageType.MESSAGE, "3"));
		registry.register("success.action", new DefaultMessageEntry(MessageType.MESSAGE, "4"));

		Set<String> errorKeys = registry.getKeysByPrefix("errors");
		assertEquals(3, errorKeys.size());

		Set<String> permissionKeys = registry.getKeysByPrefix("errors.permission");
		assertEquals(2, permissionKeys.size());

		Set<String> databaseKeys = registry.getKeysByPrefix("errors.database");
		assertEquals(1, databaseKeys.size());
	}

	@Test
	void shouldGetAllEntries() {
		registry.register("key1", new DefaultMessageEntry(MessageType.MESSAGE, "1"));
		registry.register("key2", new DefaultMessageEntry(MessageType.TEMPLATE, "2"));

		Map<String, MessageEntry> entries = registry.getAllEntries();
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
		registry.register("key", new DefaultMessageEntry(MessageType.MESSAGE, "First"));
		registry.register("key", new DefaultMessageEntry(MessageType.MESSAGE, "Second"));

		MessageEntry entry = registry.get("key");
		assertEquals("Second", entry.getText());
	}
}