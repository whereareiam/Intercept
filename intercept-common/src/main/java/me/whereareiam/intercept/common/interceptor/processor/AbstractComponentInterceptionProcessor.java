package me.whereareiam.intercept.common.interceptor.processor;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.common.util.ComponentHelper;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.logging.InterceptionHelper;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import me.whereareiam.intercept.model.regex.MatchDetails;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Abstract base class for component interception processors.
 * Contains all common business logic for component interception, tag replacement, and message processing.
 * <p>
 * Processing flow:
 * 1. Check if component interception is enabled
 * 2. Extract plain text from component
 * 3. Check for tag (configurable format) - if found, use tag-based processing
 * 4. If no tag and regex enabled, try regex pattern matching
 * 5. If regex matches, replace component text with resolved message
 * <p>
 * Tag syntax: {@code tagFormat key="message.key" param1="value1" param2="value2"}
 * <p>
 * The tag format is configurable. Common examples:
 * <ul>
 *   <li>{@code <lang key="player.join" name="Steve">} - XML-like format (tag: {@code <lang>})</li>
 *   <li>{@code [l key="player.join" name="Steve"]} - Square brackets (tag: {@code [l]})</li>
 *   <li>{@code {tr key="player.join" name="Steve"}} - Curly braces (tag: {@code {tr}})</li>
 * </ul>
 *
 * @param <T> The specific interception context type
 */
@RequiredArgsConstructor
public abstract class AbstractComponentInterceptionProcessor<T extends InterceptionContext> {
	protected final Provider<Interception> interceptionProvider;
	protected final Provider<Settings> settingsProvider;
	protected final RegexMatchingService regexMatchingService;
	protected final TagReplacementService tagReplacementService;

	/**
	 * Processes a component message using the common interception logic.
	 *
	 * @param context The interception context containing extracted data
	 * @return The processed message to write back, or null if no changes
	 */
	@Nullable
	protected Component process(T context) {
		Interception interception = interceptionProvider.get();
		if (interception == null || interception.getComponents() == null) {
			return null;
		}

		// Get component configuration
		InterceptedComponent componentConfig = interception.getComponents().get(getComponentType());
		if (componentConfig == null || !componentConfig.isEnabled()) {
			return null;
		}

		Component message = context.getMessage();
		String plainText = ComponentHelper.extractPlainText(message);

		// Step 1: Check for tag (primary method)
		if (componentConfig.getTag() != null && TagParser.containsTag(plainText, componentConfig.getTag())) {
			// Tag found - use tag-based processing
			Component processed = tagReplacementService.replaceTags(
					message,
					componentConfig.getTag(),
					context.getLocale(),
					getComponentType()
			);

			return InterceptionHelper.modify(processed);
		}

		// Step 2: Try regex matching (fallback method)
		if (componentConfig.isRegex() && isRegexEnabled()) {
			Optional<MatchDetails> matchDetails = regexMatchingService.matchWithDetails(plainText, context.getLocale());

			if (matchDetails.isPresent()) {
				MatchDetails details = matchDetails.get();
				Component resolved;

				if (details.isReplaceMatched()) {
					// Replace only the matched part, preserving formatting
					resolved = ComponentHelper.replaceTextRange(
							message,
							details.getMatchStart(),
							details.getMatchEnd(),
							details.getResolvedText()
					);
				} else {
					// Replace entire message (backward compatibility)
					resolved = ComponentHelper.replaceEntireText(details.getResolvedText());
				}

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

	/**
	 * Gets the component type that this processor handles.
	 * Also used as the source context for message resolution and fallback formatting.
	 *
	 * @return The component type
	 */
	protected abstract ComponentType getComponentType();
}

