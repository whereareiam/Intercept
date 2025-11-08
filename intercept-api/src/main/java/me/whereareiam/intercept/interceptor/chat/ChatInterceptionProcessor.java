package me.whereareiam.intercept.interceptor.chat;

import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Processor for chat components. Contains all business logic for processing chat messages.
 * Implemented in the common module and called by platform-specific interceptors.
 */
public interface ChatInterceptionProcessor {
	/**
	 * Processes a chat message.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed message to write back, or null if no changes
	 */
	@Nullable
	Component processChat(ChatInterceptionContext context);
}