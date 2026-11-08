package me.whereareiam.intercept.persistence.database;

import me.whereareiam.dialectica.schema.Schema;
import me.whereareiam.strata.Migration;
import me.whereareiam.strata.adapter.database.DatabaseContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The tables of one owner, as a Strata migration stream. A new database gets {@link #schema()} at the latest version;
 * an existing one gets the {@link #migrations()} it has not applied yet.
 */
public interface DatabaseSchema {
	/**
	 * Returns the stream's name, which the database remembers its version under.
	 *
	 * @return the name, such as {@code intercept/shared}
	 */
	@NotNull String stream();

	/**
	 * Returns the tables at the latest version.
	 *
	 * @return the schema
	 */
	@NotNull Schema schema();

	/**
	 * Returns the steps from each earlier version to the next.
	 *
	 * @return the migrations, empty while the schema never changed
	 */
	default @NotNull List<Migration<DatabaseContext>> migrations() {
		return List.of();
	}
}
