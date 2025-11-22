package me.whereareiam.intercept.adapter.database.entity;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.schema.SchemaProvider;
import me.whereareiam.intercept.type.PersistenceType;

import java.util.UUID;

/**
 * ORMLite entity representing a player's persistent data.
 * Stores player state such as inspection mode, locale, and other preferences.
 */
@Getter
@Setter
public class PlayerEntity implements SchemaProvider {
	/**
	 * The player's unique identifier (UUID).
	 * Used as the primary key.
	 */
	private UUID uniqueId;

	/**
	 * Whether inspection mode is enabled for this player.
	 * When enabled, chat messages become clickable and show regex patterns.
	 */
	private boolean inspectionMode;

	@Override
	public String getTableName() {
		return Constants.Database.Tables.PLAYERS;
	}

	@Override
	public String getCreateTableStatement(PersistenceType persistenceType) {
		return """
				CREATE TABLE IF NOT EXISTS %s (
					unique_id CHAR(36) PRIMARY KEY,
					inspection_mode BOOLEAN NOT NULL DEFAULT FALSE
				)
				""".formatted(getTableName());
	}
}

