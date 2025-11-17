package me.whereareiam.intercept.common.interceptor.processor;

import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.interception.ProcessedEvent;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for interception processors that fire events after processing.
 * Handles the common pattern of processing a component, firing an event, and returning
 * the potentially modified component from the event.
 * <p>
 * This is a general-purpose base class that can be extended by more specific processors.
 *
 * @param <T> The specific interception context type
 * @param <E> The processed event type that extends ProcessedEvent
 */
public abstract class InterceptionProcessor<T extends InterceptionContext, E extends ProcessedEvent> {
	protected final EventManager eventManager;

	/**
	 * Creates a new InterceptionProcessor.
	 *
	 * @param eventManager Manager for firing events
	 */
	protected InterceptionProcessor(@NotNull EventManager eventManager) {
		this.eventManager = eventManager;
	}

	/**
	 * Processes the interception context and fires the appropriate event.
	 * This method handles the common pattern: process -> fire event -> return modified component.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed and potentially modified component, or null if no changes
	 */
	@Nullable
	protected Component processWithEvent(T context) {
		Component processed = process(context);

		// If no processing was done, use original message
		Component messageToReturn = processed != null ? processed : context.getMessage();

		// Create and fire the event
		E event = createEvent(context, messageToReturn);
		eventManager.call(event);

		// Return the potentially modified component from the event
		return event.getComponent();
	}

	/**
	 * Processes the interception context.
	 * Subclasses must implement this to provide their specific processing logic.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed message to write back, or null if no changes
	 */
	@Nullable
	protected abstract Component process(T context);

	/**
	 * Creates the processed event for the given context and component.
	 * Subclasses must implement this to create their specific event type.
	 *
	 * @param context   The interception context
	 * @param component The component to include in the event
	 * @return The created event instance
	 */
	@NotNull
	protected abstract E createEvent(T context, Component component);
}