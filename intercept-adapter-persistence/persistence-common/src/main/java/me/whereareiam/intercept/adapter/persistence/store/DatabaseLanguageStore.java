package me.whereareiam.intercept.adapter.persistence.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.adapter.persistence.entity.LanguageEntity;
import me.whereareiam.intercept.persistence.LanguageStore;
import me.whereareiam.intercept.persistence.database.DatabaseService;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps players' language choices in one table.
 */
@Singleton
public final class DatabaseLanguageStore implements LanguageStore {
	private final DatabaseService database;

	@Inject
	public DatabaseLanguageStore(@NotNull DatabaseService database) {
		this.database = database;
	}

	@Override
	public @NotNull Optional<Locale> find(@NotNull UUID uniqueId) {
		return database.access(LanguageEntity.class)
				.find(uniqueId)
				.map(entity -> Locale.forLanguageTag(entity.getLanguage()));
	}

	@Override
	public void save(@NotNull UUID uniqueId, @NotNull Locale language) {
		database.access(LanguageEntity.class)
				.upsert(new LanguageEntity(uniqueId, language.toLanguageTag(), System.currentTimeMillis()));
	}

	@Override
	public void delete(@NotNull UUID uniqueId) {
		database.access(LanguageEntity.class).delete(uniqueId);
	}
}
