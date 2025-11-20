package me.whereareiam.intercept.adapter.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * ORMLite entity representing a player's persistent data.
 * Stores player state such as inspection mode, locale, and other preferences.
 */
@Getter
@Setter
@DatabaseTable(tableName = "intercept_players")
public class PlayerEntity {
	/**
	 * The player's unique identifier (UUID).
	 * Used as the primary key.
	 */
	@DatabaseField(id = true, dataType = DataType.UUID)
	private UUID uniqueId;

	/**
	 * Whether inspection mode is enabled for this player.
	 * When enabled, chat messages become clickable and show regex patterns.
	 */
	@DatabaseField
	private boolean inspectionMode;
}

