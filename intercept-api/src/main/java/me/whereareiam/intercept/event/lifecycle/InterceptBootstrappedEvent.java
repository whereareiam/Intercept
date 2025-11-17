package me.whereareiam.intercept.event.lifecycle;

import me.whereareiam.intercept.event.base.SynchronousEvent;

/**
 * Event called when the Intercept plugin finishes bootstrapping.
 * This event is triggered at the end of the onLoad() lifecycle method,
 * after the platform-specific infrastructure, dependency injection, and core systems
 * have been initialized.
 * <p>
 * At this point, the injector is available and dependencies can be resolved,
 * but the plugin is not yet fully operational.
 * <p>
 * This event is useful for modules or components that need to perform
 * initialization tasks that depend on the core infrastructure being ready.
 */
public class InterceptBootstrappedEvent implements SynchronousEvent {
}

