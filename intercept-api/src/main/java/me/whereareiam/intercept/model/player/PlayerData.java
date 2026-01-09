package me.whereareiam.intercept.model.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;
import java.util.UUID;

/**
 * Data transfer object for player persistent data.
 * Used for transferring player information between memory and database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerData {
	/**
	 * The player's unique identifier.
	 */
	private UUID uniqueId;

	/**
	 * Whether inspection mode is enabled for this player.
	 */
	private boolean inspectionMode;

	/**
	 * The player's custom locale override.
	 * If null, the player's client locale from Minecraft will be used.
	 */
	private Locale locale;
}