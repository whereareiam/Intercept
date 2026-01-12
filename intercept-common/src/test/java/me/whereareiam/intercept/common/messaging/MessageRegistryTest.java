package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.SemanticaTestHelper;
import me.whereareiam.intercept.common.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.registry.InterceptTranslationRegistry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageRegistryTest {
	@Mock
	private Registry<Reloadable> reloadableRegistry;
	private DefaultMessageRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new DefaultMessageRegistry(new InterceptTranslationRegistry(), reloadableRegistry);
	}

	@Test
	void shouldRegisterAndGetEntry() {
		TranslationEntry entry = SemanticaTestHelper.template("Test");
		registry.register("test.key", entry);

		TranslationEntry retrieved = registry.get("test.key");
		assertNotNull(retrieved);
		assertEquals("Test", ((TemplateEntry) retrieved).getTemplate());
	}

	@Test
	void shouldReturnNullForNonExistentKey() {
		TranslationEntry entry = registry.get("nonexistent");
		assertNull(entry);
	}

	@Test
	void shouldCheckIfKeyExists() {
		TranslationEntry entry = SemanticaTestHelper.template("Test");
		registry.register("test.key", entry);

		assertTrue(registry.exists("test.key"));
		assertFalse(registry.exists("other.key"));
	}

	@Test
	void shouldGetAllKeys() {
		registry.register("key1", SemanticaTestHelper.template("1"));
		registry.register("key2", SemanticaTestHelper.template("2"));
		registry.register("key3", SemanticaTestHelper.template("3"));

		Set<String> keys = registry.getKeys();
		assertEquals(3, keys.size());
		assertTrue(keys.contains("key1"));
		assertTrue(keys.contains("key2"));
		assertTrue(keys.contains("key3"));
	}

	@Test
	void shouldGetKeysByPrefix() {
		registry.register("errors.permission1", SemanticaTestHelper.template("1"));
		registry.register("errors.permission2", SemanticaTestHelper.template("2"));
		registry.register("errors.database.connection", SemanticaTestHelper.template("3"));
		registry.register("success.action", SemanticaTestHelper.template("4"));

		Set<String> errorKeys = registry.getKeys("errors");
		assertEquals(3, errorKeys.size());

		Set<String> permissionKeys = registry.getKeys("errors.permission");
		assertEquals(2, permissionKeys.size());

		Set<String> databaseKeys = registry.getKeys("errors.database");
		assertEquals(1, databaseKeys.size());
	}

	@Test
	void shouldGetAllEntries() {
		registry.register("key1", SemanticaTestHelper.template("1"));
		registry.register("key2", SemanticaTestHelper.template("2"));

		Map<String, TranslationEntry> entries = registry.getAllEntries();
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
		registry.register("key", SemanticaTestHelper.template("First"));
		registry.register("key", SemanticaTestHelper.template("Second"));

		TranslationEntry entry = registry.get("key");
		assertEquals("Second", ((TemplateEntry) entry).getTemplate());
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(registry);
	}

	@Test
	void shouldClearAllEntriesOnReload() {
		// Register some entries
		registry.register("key1", SemanticaTestHelper.template("Namespace 1"));
		registry.register("key2", SemanticaTestHelper.template("Namespace 2"));
		registry.register("key3", SemanticaTestHelper.template("Namespace 3"));

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
		registry.register("key1", SemanticaTestHelper.template("Original"));
		assertEquals("Original", ((TemplateEntry) registry.get("key1")).getTemplate());

		// Reload
		registry.reload();

		// Register new entries
		registry.register("key1", SemanticaTestHelper.template("New"));
		registry.register("key2", SemanticaTestHelper.template("Additional"));

		assertEquals("New", ((TemplateEntry) registry.get("key1")).getTemplate());
		assertEquals("Additional", ((TemplateEntry) registry.get("key2")).getTemplate());
		assertEquals(2, registry.getKeys().size());
	}
}
