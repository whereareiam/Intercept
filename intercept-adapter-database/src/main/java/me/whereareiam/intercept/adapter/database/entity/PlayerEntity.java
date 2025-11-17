package me.whereareiam.intercept.adapter.database.entity;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Ebean entity representing a player's persistent data.
 * Stores player state such as inspection mode, locale, and other preferences.
 */
@Getter
@Entity
@Table(name = "players")
public class PlayerEntity extends Model {
	/**
	 * The player's unique identifier (UUID).
	 * Used as the primary key.
	 */
	@Setter
	@Id
	private UUID uniqueId;

	/**
	 * Whether inspection mode is enabled for this player.
	 * When enabled, chat messages become clickable and show regex patterns.
	 */
	private boolean inspectionMode;
}

