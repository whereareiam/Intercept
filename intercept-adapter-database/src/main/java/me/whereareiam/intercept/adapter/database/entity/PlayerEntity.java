package me.whereareiam.intercept.adapter.database.entity;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.dialectica.EntitySchemaProvider;
import me.whereareiam.dialectica.annotation.Entity;
import me.whereareiam.dialectica.type.DatabaseType;

import java.util.UUID;

/**
 * ORMLite entity representing a player's persistent data.
 * Stores player state such as inspection mode, locale, and other preferences.
 */
@Getter
@Setter
@Entity(tableName = "intercept_players", version = 1)
public class PlayerEntity implements EntitySchemaProvider {
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
	public String statement(DatabaseType databaseType) {
		return """
				CREATE TABLE IF NOT EXISTS intercept_players (
					unique_id CHAR(36) PRIMARY KEY,
					inspection_mode BOOLEAN NOT NULL DEFAULT FALSE
				)
				""";
	}
}