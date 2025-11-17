package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.interception.kick.KickProcessedEvent;
import me.whereareiam.intercept.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.kick.KickInterceptionContext;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of kick interception processing.
 * Delegates all common processing logic to {@link AbstractComponentInterceptionProcessor}.
 */
@Singleton
public class DefaultKickInterceptionProcessor
		extends AbstractComponentInterceptionProcessor<KickInterceptionContext, KickProcessedEvent>
		implements KickInterceptionProcessor {

	@Inject
	public DefaultKickInterceptionProcessor(
			Provider<Interception> interceptionProvider,
			Provider<Settings> settingsProvider,
			RegexMatchingService regexMatchingService,
			TagReplacementService tagReplacementService,
			EventManager eventManager
	) {
		super(interceptionProvider, settingsProvider, regexMatchingService, tagReplacementService, eventManager);
	}

	@Override
	@Nullable
	public Component processKick(KickInterceptionContext context) {
		return processWithEvent(context);
	}

	@Override
	@NotNull
	protected KickProcessedEvent createEvent(KickInterceptionContext context, Component component) {
		return new KickProcessedEvent(context, component);
	}

	@Override
	protected ComponentType getComponentType() {
		return ComponentType.KICK;
	}
}