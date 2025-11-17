package me.whereareiam.intercept.event.lifecycle;

import me.whereareiam.intercept.event.base.SynchronousEvent;

/**
 * Event called when the Intercept plugin is fully ready.
 * This event is triggered at the end of the onEnable() lifecycle method,
 * after all platform initialization and startup tasks have been completed.
 * <p>
 * At this point, the plugin's core systems are enabled, but the Intercept class
 * may still be performing its initialization.
 * <p>
 * This event is useful for modules or components that need to perform
 * actions after the plugin is enabled but before the complete startup.
 */
public class InterceptReadyEvent implements SynchronousEvent {
}

