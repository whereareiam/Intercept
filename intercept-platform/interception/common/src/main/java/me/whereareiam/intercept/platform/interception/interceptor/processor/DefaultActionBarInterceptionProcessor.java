package me.whereareiam.intercept.platform.interception.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.interception.actionbar.ActionBarProcessedEvent;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.interception.actionbar.ActionBarInterceptionContext;
import me.whereareiam.intercept.platform.interception.regex.RegexMatchingService;
import me.whereareiam.intercept.platform.interception.tag.TagReplacementService;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of action bar interception processing.
 * Delegates all common processing logic to {@link AbstractComponentInterceptionProcessor}.
 */
@Singleton
public class DefaultActionBarInterceptionProcessor
		extends AbstractComponentInterceptionProcessor<ActionBarInterceptionContext, ActionBarProcessedEvent>
		implements ActionBarInterceptionProcessor {

	@Inject
	public DefaultActionBarInterceptionProcessor(
			Provider<Interception> interceptionProvider,
			RegexMatchingService regexMatchingService,
			TagReplacementService tagReplacementService,
			EventManager eventManager
	) {
		super(interceptionProvider, regexMatchingService, tagReplacementService, eventManager);
	}

	@Override
	@Nullable
	public Component processActionBar(ActionBarInterceptionContext context) {
		return processWithEvent(context);
	}

	@Override
	@NotNull
	protected ActionBarProcessedEvent createEvent(ActionBarInterceptionContext context, Component component) {
		return new ActionBarProcessedEvent(context, component);
	}

	@Override
	protected ComponentType getComponentType() {
		return ComponentType.ACTION_BAR;
	}
}

