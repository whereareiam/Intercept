package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.regex.RegexMatchingService;
import me.whereareiam.intercept.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.actionbar.ActionBarInterceptionContext;
import me.whereareiam.intercept.type.InterceptedComponentType;
import me.whereareiam.intercept.type.message.MessageSource;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of action bar interception processing.
 * Delegates all common processing logic to {@link AbstractComponentInterceptionProcessor}.
 */
@Singleton
public class DefaultActionBarInterceptionProcessor
		extends AbstractComponentInterceptionProcessor<ActionBarInterceptionContext>
		implements ActionBarInterceptionProcessor {

	@Inject
	public DefaultActionBarInterceptionProcessor(
			Provider<Interception> interceptionProvider,
			Provider<Settings> settingsProvider,
			RegexMatchingService regexMatchingService,
			TagReplacementService tagReplacementService
	) {
		super(interceptionProvider, settingsProvider, regexMatchingService, tagReplacementService);
	}

	@Override
	@Nullable
	public Component processActionBar(ActionBarInterceptionContext context) {
		return process(context);
	}

	@Override
	protected InterceptedComponentType getComponentType() {
		return InterceptedComponentType.ACTION_BAR;
	}

	@Override
	protected MessageSource getMessageSource() {
		return MessageSource.ACTION_BAR;
	}
}

