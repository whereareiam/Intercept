package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.regex.RegexMatchingService;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of chat interception processing.
 * Delegates all common processing logic to {@link AbstractComponentInterceptionProcessor}.
 */
@Singleton
public class DefaultChatInterceptionProcessor
		extends AbstractComponentInterceptionProcessor<ChatInterceptionContext>
		implements ChatInterceptionProcessor {

	@Inject
	public DefaultChatInterceptionProcessor(
			Provider<Interception> interceptionProvider,
			Provider<Settings> settingsProvider,
			RegexMatchingService regexMatchingService,
			TagReplacementService tagReplacementService
	) {
		super(interceptionProvider, settingsProvider, regexMatchingService, tagReplacementService);
	}

	@Override
	@Nullable
	public Component processChat(ChatInterceptionContext context) {
		return process(context);
	}

	@Override
	protected ComponentType getComponentType() {
		return ComponentType.CHAT;
	}
}