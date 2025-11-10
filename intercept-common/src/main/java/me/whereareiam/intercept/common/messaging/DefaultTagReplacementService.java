package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.util.ComponentHelper;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of TagReplacementService.
 * Handles tag parsing, message resolution, and component reconstruction while preserving formatting.
 */
@Singleton
public class DefaultTagReplacementService implements TagReplacementService {
	private final MessageService messageService;

	@Inject
	public DefaultTagReplacementService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public Component replaceTags(Component component, String tagFormat, Locale locale) {
		// Extract plain text from component
		String plainText = ComponentHelper.extractPlainText(component);

		// Find all tags in the text
		List<ComponentHelper.TagData> tags = ComponentHelper.extractTags(plainText, tagFormat);

		if (tags.isEmpty()) return component;

		// Build replacement map
		Map<String, String> replacements = new HashMap<>();

		for (ComponentHelper.TagData tag : tags) {
			try {
				// Convert placeholders to Map<String, Object>
				Map<String, Object> placeholders = new HashMap<>();
				for (ComponentHelper.TagData.Placeholder placeholder : tag.getPlaceholders()) {
					placeholders.put(placeholder.getName(), placeholder.getValue());
				}

				// Resolve the message
				String resolved = messageService.resolve(tag.getKey(), locale, placeholders);

				if (resolved != null) {
					// Add to replacement map
					replacements.put(tag.getOriginalTag(), resolved);
					continue;
				}

				Logger.warn("Failed to resolve message key: %s for locale: %s", tag.getKey(), locale);
				// Use key as fallback
				replacements.put(tag.getOriginalTag(), tag.getKey());
			} catch (Exception e) {
				Logger.warn("Error resolving tag '%s': %s", tag.getOriginalTag(), e.getMessage());
				// Keep original tag on error
				replacements.put(tag.getOriginalTag(), tag.getOriginalTag());
			}
		}

		// Apply replacements to component
		return ComponentHelper.replaceTextInComponent(component, replacements);
	}

	@Override
	public boolean containsTags(Component component, String tagFormat) {
		return ComponentHelper.containsTag(component, tagFormat);
	}
}