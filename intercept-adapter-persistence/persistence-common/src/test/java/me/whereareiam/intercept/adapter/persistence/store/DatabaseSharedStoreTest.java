package me.whereareiam.intercept.adapter.persistence.store;

import me.whereareiam.intercept.adapter.persistence.DefaultDatabaseService;
import me.whereareiam.intercept.adapter.persistence.TestDatabases;
import me.whereareiam.intercept.model.shared.SharedChanges;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedRule;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.intercept.persistence.database.DatabaseType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("database")
class DatabaseSharedStoreTest {
	private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");

	@TempDir
	Path folder;

	@ParameterizedTest
	@MethodSource("me.whereareiam.intercept.adapter.persistence.TestDatabases#types")
	void aPublicationRaisesTheRevisionAndReplacesRows(DatabaseType type) {
		DefaultDatabaseService database = TestDatabases.open(type, folder);
		try {
			DatabaseSharedStore store = new DatabaseSharedStore(database);
			assertEquals(0, store.revision());

			long first = store.apply(new SharedChanges(
					List.of(message("server.format.prefix", "A » "), message("essentials.motd", "Hi")),
					List.of(),
					List.of(new SharedRule("essentials.yml", "x: 1\n", "Alice", NOW)),
					List.of()
			));
			long second = store.apply(new SharedChanges(
					List.of(message("server.format.prefix", "B » ")),
					List.of(new SharedChanges.MessageKey(Locale.ENGLISH, "essentials.motd")),
					List.of(),
					List.of("essentials.yml")
			));

			SharedSnapshot snapshot = store.load();
			assertEquals(1, first);
			assertEquals(2, second);
			assertEquals(2, snapshot.getRevision());
			assertEquals(1, snapshot.getMessages().size());
			assertEquals("B » ", snapshot.getMessages().getFirst().getText());
			assertEquals(NOW, snapshot.getMessages().getFirst().getChangedAt());
			assertTrue(snapshot.getRules().isEmpty());
		} finally {
			database.stop();
		}
	}

	@ParameterizedTest
	@MethodSource("me.whereareiam.intercept.adapter.persistence.TestDatabases#types")
	void aMessageForEveryLanguageKeepsItsLanguage(DatabaseType type) {
		DefaultDatabaseService database = TestDatabases.open(type, folder);
		try {
			DatabaseSharedStore store = new DatabaseSharedStore(database);
			SharedMessage link = new SharedMessage(Locale.ROOT, "identica.login.failed", "identica.yml", "{@game.account.login-failed}", "Alice", NOW);

			store.apply(new SharedChanges(List.of(link), List.of(), List.of(), List.of()));

			assertEquals(Locale.ROOT, store.load().getMessages().getFirst().getLanguage());
		} finally {
			database.stop();
		}
	}

	@ParameterizedTest
	@MethodSource("me.whereareiam.intercept.adapter.persistence.TestDatabases#types")
	void emptyChangesLeaveTheRevision(DatabaseType type) {
		DefaultDatabaseService database = TestDatabases.open(type, folder);
		try {
			DatabaseSharedStore store = new DatabaseSharedStore(database);

			assertEquals(0, store.apply(new SharedChanges(List.of(), List.of(), List.of(), List.of())));
		} finally {
			database.stop();
		}
	}

	private static SharedMessage message(String key, String text) {
		return new SharedMessage(Locale.ENGLISH, key, key.substring(0, key.indexOf('.')) + ".yml", text, "Alice", NOW);
	}
}
