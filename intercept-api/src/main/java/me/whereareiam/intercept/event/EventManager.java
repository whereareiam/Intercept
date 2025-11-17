package me.whereareiam.intercept.event;


import me.whereareiam.intercept.event.base.Event;
import me.whereareiam.intercept.event.base.EventOrder;

import java.lang.reflect.Method;

/**
 * Central interface for managing event registration and dispatch in the Intercept plugin.
 * This manager provides functionality to register and unregister event listeners, and to call events.
 * <p>
 * The event system consists of several components:
 * - {@link Event}: Base interface for all events
 * - {@link EventListener}: Marker interface for classes that listen to events
 * - {@link EventOrder}: Defines the order in which event handlers are called
 */
public interface EventManager {
	/**
	 * Registers an event listener class with the event system.
	 *
	 * @param eventListener the listener instance to register
	 */
	void register(EventListener eventListener);

	/**
	 * Registers a specific method as an event handler with a defined order.
	 *
	 * @param event    the event class to listen for
	 * @param listener the instance containing the handler method
	 * @param method   the method to be called when the event occurs
	 * @param order    the order in which this handler should be called
	 * @param <T>      the type of event
	 */
	<T extends Event> void registerListener(Class<T> event, Object listener, Method method, EventOrder order);

	/**
	 * Unregisters an event listener from the event system.
	 *
	 * @param eventListener the listener instance to unregister
	 */
	void unregister(EventListener eventListener);

	/**
	 * Calls an event, triggering all registered handlers for that event type.
	 * Handlers are called in order according to their {@link EventOrder}.
	 * <p>
	 * Events implementing {@link me.whereareiam.intercept.event.base.SynchronousEvent}
	 * will be processed synchronously on the current thread. All other events
	 * will be processed asynchronously.
	 *
	 * @param event the event to call
	 */
	void call(Event event);
}