package me.whereareiam.intercept.interceptor.actionbar;

import me.whereareiam.intercept.model.interception.actionbar.ActionBarInterceptionContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Processor for action bar components. Contains all business logic for processing action bar messages.
 * Implemented in the common module and called by platform-specific interceptors.
 */
public interface ActionBarInterceptionProcessor {
	/**
	 * Processes an action bar message.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed message to write back, or null if no changes
	 */
	@Nullable
	Component processActionBar(ActionBarInterceptionContext context);
}