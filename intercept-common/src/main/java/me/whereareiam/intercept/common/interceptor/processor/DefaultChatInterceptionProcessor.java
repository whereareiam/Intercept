package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.common.messaging.regex.RegexMatchingService;
import me.whereareiam.intercept.common.util.ComponentHelper;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.logging.InterceptionHelper;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.chat.ChatInterceptionContext;
import me.whereareiam.intercept.type.InterceptedComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Default implementation of chat interception processing.
 * Contains all business logic for chat interception, tag replacement, and message processing.
 * <p>
 * Processing flow:
 * 1. Check if component interception is enabled
 * 2. Extract plain text from component
 * 3. Check for tag (e.g., "&lt;lang&gt;") - if found, use tag-based processing
 * 4. If no tag and regex enabled, try regex pattern matching
 * 5. If regex matches, replace component text with resolved message
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultChatInterceptionProcessor implements ChatInterceptionProcessor {
	private final Provider<Interception> interceptionProvider;
	private final Provider<Settings> settingsProvider;
	private final RegexMatchingService regexMatchingService;

	@Override
	@Nullable
	public Component processChat(ChatInterceptionContext context) {
		Interception interception = interceptionProvider.get();
		if (interception == null || interception.getComponents() == null) {
			return null;
		}

		// Get chat component configuration
		InterceptedComponent chatConfig = interception.getComponents().get(InterceptedComponentType.CHAT);
		if (chatConfig == null || !chatConfig.isEnabled()) return null;

		Component message = context.getMessage();
		String plainText = ComponentHelper.extractPlainText(message);

		// Step 1: Check for tag (primary method)
		if (chatConfig.getTag() != null && ComponentHelper.containsTag(message, chatConfig.getTag())) {
			// Tag found - use tag-based processing
			// TODO
			return InterceptionHelper.modify(message);
		}

		// Step 2: Try regex matching (fallback method)
		if (chatConfig.isRegex() && isRegexEnabled()) {
			Optional<String> regexMatch = regexMatchingService.match(plainText, context.getLocale());

			if (regexMatch.isPresent()) {
				// Regex matched! Replace the entire message with resolved text
				Component resolved = ComponentHelper.replaceEntireText(regexMatch.get());
				return InterceptionHelper.modify(resolved);
			}
		}

		// No interception performed
		return null;
	}

	/**
	 * Check if regex matching is enabled globally.
	 */
	private boolean isRegexEnabled() {
		return settingsProvider.get().getPerformance().getRegex().isEnabled();
	}
}