package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of chat interception processing.
 * Contains all business logic for chat interception, tag replacement, and message processing.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultChatInterceptionProcessor implements ChatInterceptionProcessor {
	private final Provider<Interception> interceptionProvider;

	@Override
	@Nullable
	public Component processChat(ChatInterceptionContext context) {
		Interception interception = interceptionProvider.get();
		if (interception == null || interception.getComponents() == null)
			return null;

		Component message = context.getMessage();

		return message.append(Component.text(" [INTERCEPTED]"));
	}
}