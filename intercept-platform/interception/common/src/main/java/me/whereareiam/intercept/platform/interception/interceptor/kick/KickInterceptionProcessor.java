package me.whereareiam.intercept.platform.interception.interceptor.kick;

import me.whereareiam.intercept.model.interception.kick.KickInterceptionContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Processor for kick/disconnect components. Contains all business logic for processing kick messages.
 * Implemented in the common module and called by platform-specific interceptors.
 */
public interface KickInterceptionProcessor {
	/**
	 * Processes a kick/disconnect message.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed message to write back, or null if no changes
	 */
	@Nullable
	Component processKick(KickInterceptionContext context);
}