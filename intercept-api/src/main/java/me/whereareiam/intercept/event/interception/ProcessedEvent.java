package me.whereareiam.intercept.event.interception;

import me.whereareiam.intercept.event.base.SynchronousEvent;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import net.kyori.adventure.text.Component;

/**
 * Interface for events that contain a processable component.
 * Events implementing this interface can have their component modified by listeners.
 */
public interface ProcessedEvent extends SynchronousEvent {
	/**
	 * Gets the interception context from this event.
	 *
	 * @return The interception context
	 */
	InterceptionContext getContext();

	/**
	 * Gets the component from this event.
	 *
	 * @return The component
	 */
	Component getComponent();

	/**
	 * Sets the component for this event.
	 *
	 * @param component The new component
	 */
	void setComponent(Component component);
}