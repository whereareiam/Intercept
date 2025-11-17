package me.whereareiam.intercept.event.base;

/**
 * Marker interface for events that must be processed synchronously.
 * Events implementing this interface will be executed on the current thread
 * instead of asynchronously, ensuring that modifications to the event object
 * are immediately available to the caller.
 * <p>
 * This is useful for events where the caller needs to read modified state
 * from the event object after processing, such as component modification events.
 */
public interface SynchronousEvent extends Event {
}