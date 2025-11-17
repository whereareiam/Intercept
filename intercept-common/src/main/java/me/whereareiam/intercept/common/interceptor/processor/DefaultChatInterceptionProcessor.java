package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.interception.chat.ChatProcessedEvent;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of chat interception processing.
 * Delegates all common processing logic to {@link AbstractComponentInterceptionProcessor}.
 */
@Singleton
public class DefaultChatInterceptionProcessor
		extends AbstractComponentInterceptionProcessor<ChatInterceptionContext, ChatProcessedEvent>
		implements ChatInterceptionProcessor {

	@Inject
	public DefaultChatInterceptionProcessor(
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
	public Component processChat(ChatInterceptionContext context) {
		return processWithEvent(context);
	}

	@Override
	@NotNull
	protected ChatProcessedEvent createEvent(ChatInterceptionContext context, Component component) {
		return new ChatProcessedEvent(context, component);
	}

	@Override
	protected ComponentType getComponentType() {
		return ComponentType.CHAT;
	}
}