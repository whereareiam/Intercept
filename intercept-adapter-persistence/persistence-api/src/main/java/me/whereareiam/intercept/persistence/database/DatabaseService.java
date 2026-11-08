package me.whereareiam.intercept.persistence.database;

import me.whereareiam.dialectica.adapter.jdbi.entity.EntityAccess;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

/**
 * Intercept's database connection, open from the persistence phase until Intercept stops.
 */
public interface DatabaseService {
	/**
	 * Returns the connection.
	 *
	 * @return Jdbi on the configured database
	 * @throws IllegalStateException before the database is open
	 */
	@NotNull Jdbi jdbi();

	/**
	 * Returns reads and writes of one entity, declared with Dialectica's annotations.
	 *
	 * <pre>{@code
	 * EntityAccess<LanguageEntity> languages = database.access(LanguageEntity.class);
	 * languages.upsert(new LanguageEntity(uniqueId, "de", System.currentTimeMillis()));
	 * }</pre>
	 *
	 * @param type the entity class
	 * @param <T>  the entity type
	 * @return the access
	 */
	<T> @NotNull EntityAccess<T> access(@NotNull Class<T> type);

	/**
	 * Brings a schema up to date: creates its tables on a new database and applies its migrations otherwise.
	 *
	 * @param schema the schema
	 */
	void install(@NotNull DatabaseSchema schema);
}
