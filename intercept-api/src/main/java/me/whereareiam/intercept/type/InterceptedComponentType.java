package me.whereareiam.intercept.type;

/**
 * Enum representing different types of Minecraft components that can be intercepted.
 */
public enum InterceptedComponentType {
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
	ACTION_BAR
}