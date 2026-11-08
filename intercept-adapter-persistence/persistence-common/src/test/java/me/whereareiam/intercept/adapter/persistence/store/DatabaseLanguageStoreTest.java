package me.whereareiam.intercept.adapter.persistence.store;

import me.whereareiam.intercept.adapter.persistence.DefaultDatabaseService;
import me.whereareiam.intercept.adapter.persistence.TestDatabases;
import me.whereareiam.intercept.persistence.database.DatabaseType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("database")
class DatabaseLanguageStoreTest {
	private static final UUID ALICE = UUID.fromString("10920508-d5d8-3eed-93d2-92f193afe7d7");

	@TempDir
	Path folder;

	@ParameterizedTest
	@MethodSource("me.whereareiam.intercept.adapter.persistence.TestDatabases#types")
	void aChoiceIsStoredReplacedAndRemoved(DatabaseType type) {
		DefaultDatabaseService database = TestDatabases.open(type, folder);
		try {
			DatabaseLanguageStore store = new DatabaseLanguageStore(database);

			store.save(ALICE, Locale.GERMAN);
			store.save(ALICE, Locale.forLanguageTag("pt-BR"));
			assertEquals(Optional.of(Locale.forLanguageTag("pt-BR")), store.find(ALICE));

			store.delete(ALICE);
			assertEquals(Optional.empty(), store.find(ALICE));
		} finally {
			database.stop();
		}
	}
}
