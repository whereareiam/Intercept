package me.whereareiam.intercept.platform.interception.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.interception.ProcessedEvent;
import me.whereareiam.intercept.event.interception.chat.ChatProcessedEvent;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.interception.InterceptionContext;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.platform.interception.util.ComponentHelper;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.intercept.util.RegexHelper;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event listener that enhances messages with inspection mode functionality.
 * When a player is in inspection mode, messages become clickable and show regex patterns.
 */
@Singleton
public class InspectionModeEnhancer implements EventListener {
	private final PlayerRegistry playerRegistry;
	private final Provider<Messages> messagesProvider;

	@Inject
	public InspectionModeEnhancer(
			@NotNull EventManager eventManager,
			@NotNull PlayerRegistry playerRegistry,
			@NotNull Provider<Messages> messagesProvider
	) {
		this.playerRegistry = playerRegistry;
		this.messagesProvider = messagesProvider;
		eventManager.register(this);
	}

	@IntercepticEvent
	public void onChatProcessed(ChatProcessedEvent event) {
		enhanceIfInspectionModeEnabled(event);
	}

	/**
	 * Enhances the component if the player is in inspection mode.
	 *
	 * @param event The processed event containing the component
	 */
	private void enhanceIfInspectionModeEnabled(ProcessedEvent event) {
		UUID playerId = getPlayerId(event);
		if (playerId == null) return;

		boolean isInspectionModeEnabled = playerRegistry.getPlayerData(playerId)
				.map(InterceptPlayer::isInspectionMode)
				.orElse(false);

		if (!isInspectionModeEnabled) return;

		Component enhanced = addInspectionClickEvent(event.getComponent());
		event.setComponent(enhanced);
	}

	/**
	 * Gets the player ID from the event context.
	 *
	 * @param event The processed event
	 * @return The player ID, or null if not available
	 */
	private UUID getPlayerId(ProcessedEvent event) {
		InterceptionContext context = event.getContext();
		return context != null ? context.getPlayerId() : null;
	}

	/**
	 * Adds a click event to the component that shows the regex pattern when clicked.
	 * The regex pattern is the escaped plain text of the message.
	 *
	 * @param component The component to make clickable
	 * @return The component with click event added
	 */
	private Component addInspectionClickEvent(Component component) {
		String plainText = ComponentHelper.extractPlainText(component);
		String regexPattern = RegexHelper.escapeRegex(plainText);
		String pattern = createPattern(regexPattern);

		Messages.Commands.Inspect inspect = messagesProvider.get().getCommands().getInspect();
		Messages.Commands.Inspect.Hover hover = inspect.getHover();
		if (hover == null) return component;

		List<String> hoverTextFormat = hover.getFormat();
		int maxPatternLength = hover.getMaxPatternLength();
		String truncationIndicator = hover.getTruncationFormat();

		String displayPattern = regexPattern;
		String truncated = "";
		if (maxPatternLength > 0 && regexPattern.length() > maxPatternLength) {
			displayPattern = regexPattern.substring(0, maxPatternLength);
			truncated = truncationIndicator != null ? truncationIndicator : "...";
		}

		Component hoverText = buildHoverText(hoverTextFormat, displayPattern, regexPattern, truncated);

		Style newStyle = component.style()
				.clickEvent(ClickEvent.copyToClipboard(pattern))
				.hoverEvent(HoverEvent.showText(hoverText));

		return component.style(newStyle);
	}

	/**
	 * Builds the hover text component from the format template lines.
	 *
	 * @param formatLines    The format template lines
	 * @param displayPattern The pattern to display (may be truncated)
	 * @param fullPattern    The full pattern (always complete)
	 * @param truncated      The truncation indicator ("..." or empty)
	 * @return The formatted hover text component
	 */
	@NotNull
	private Component buildHoverText(
			@NotNull List<String> formatLines,
			@NotNull String displayPattern,
			@NotNull String fullPattern,
			@NotNull String truncated
	) {
		return Serializer.serialize(SerializerContent.builder()
				.message(String.join("\n", formatLines))
				.placeholders(Map.of(
						"pattern", displayPattern,
						"fullPattern", fullPattern,
						"truncated", truncated
				))
				.build());
	}

	/**
	 * Creates a config-friendly version of the regex pattern.
	 * Replaces newlines with \\n and double-escapes all backslashes.
	 *
	 * @param regexPattern The regex pattern to convert
	 * @return The Config-friendly pattern
	 */
	@NotNull
	private String createPattern(@NotNull String regexPattern) {
		return regexPattern
				.replace("\n", "\\n")   // Replace actual newline character with \n (two characters: backslash + n)
				.replace("\r", "")     // Remove carriage returns
				.replace("\\", "\\\\"); // Then double-escape all backslashes (including the ones we just added)
	}
}

