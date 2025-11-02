package me.whereareiam.intercept.event.lifecycle;

import me.whereareiam.intercept.event.base.Event;

/**
 * Event called when the Intercept plugin has fully started.
 * This event is triggered after the Intercept class completes all its initialization,
 * representing the final stage of the plugin startup sequence.
 * <p>
 * The complete startup sequence is:
 * <ol>
 *   <li>{@link InterceptBootstrappedEvent} - Core infrastructure initialized (onLoad complete)</li>
 *   <li>{@link InterceptReadyEvent} - Plugin enabled (onEnable complete)</li>
 *   <li>{@link InterceptStartedEvent} - Full startup complete (Intercept class initialized)</li>
 * </ol>
 * <p>
 * This event is useful for modules or components that need to ensure the entire
 * plugin has fully started before performing their actions.
 */
public class InterceptStartedEvent implements Event {
}