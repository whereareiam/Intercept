package me.whereareiam.intercept.interception;

/**
 * Where a system message is shown. Player chat is never a surface: it is signed by its sender and passes untouched.
 */
public enum Surface {
	/**
	 * A system message in chat.
	 */
	CHAT,
	/**
	 * The line above the hotbar.
	 */
	ACTION_BAR,
	/**
	 * The reason shown when a player is disconnected.
	 */
	KICK
}
