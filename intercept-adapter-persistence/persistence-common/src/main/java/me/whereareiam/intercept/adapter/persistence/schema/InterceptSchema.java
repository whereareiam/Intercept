package me.whereareiam.intercept.adapter.persistence.schema;

import me.whereareiam.dialectica.Dialectica;
import me.whereareiam.dialectica.schema.Schema;
import me.whereareiam.intercept.adapter.persistence.entity.LanguageEntity;
import me.whereareiam.intercept.adapter.persistence.entity.RevisionEntity;
import me.whereareiam.intercept.adapter.persistence.entity.SharedMessageEntity;
import me.whereareiam.intercept.adapter.persistence.entity.SharedRuleEntity;
import me.whereareiam.intercept.persistence.database.DatabaseSchema;
import org.jetbrains.annotations.NotNull;

/**
 * Intercept's own tables: what the network shares, its revision, and the languages players chose.
 */
public final class InterceptSchema implements DatabaseSchema {
	@Override
	public @NotNull String stream() {
		return "intercept";
	}

	@Override
	public @NotNull Schema schema() {
		return Schema.of(
				Dialectica.entity(RevisionEntity.class).table(),
				Dialectica.entity(SharedMessageEntity.class).table(),
				Dialectica.entity(SharedRuleEntity.class).table(),
				Dialectica.entity(LanguageEntity.class).table()
		);
	}
}
