package me.whereareiam.intercept.type;

/**
 * Enum representing different types of Minecraft components that can be intercepted.
 * Also used as source context for message resolution and fallback formatting.
 */
public enum ComponentType {
	/**
	 * Chat messages displayed in the chat area.
	 * Includes:
	 * <ul>
	 *   <li>Player chat messages</li>
	 *   <li>sendMessage() and broadcast() calls</li>
	 *   <li>Join/leave messages</li>
	 *   <li>Death messages</li>
	 *   <li>Command feedback (e.g., /give, /gamemode)</li>
	 *   <li>Achievement and advancement messages</li>
	 *   <li>All other system messages in the chat area</li>
	 * </ul>
	 * Note: The Minecraft protocol does not distinguish between these message types at the packet level.
	 */
	CHAT,

	/**
	 * Action bar messages (messages displayed above the hotbar)
	 */
	ACTION_BAR,

	/**
	 * Kick/disconnect messages sent to players when they are kicked from the server.
	 * The reason component in disconnect packets is intercepted and translated.
	 */
	KICK,

	/**
	 * Unknown or unspecified source.
	 * Used when the component type is not known or when processing messages outside interception context.
	 */
	UNKNOWN
}