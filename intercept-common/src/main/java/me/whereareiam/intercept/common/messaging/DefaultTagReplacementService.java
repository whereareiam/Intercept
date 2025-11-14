package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.util.ComponentHelper;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.keystone.model.SerializerContent;
import me.whereareiam.keystone.serializer.SerializerEngine;
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
	private final Provider<Messages> messagesProvider;
	private final Provider<SerializerEngine> serializerProvider;

	@Inject
	public DefaultTagReplacementService(
			MessageService messageService,
			Provider<Messages> messagesProvider,
			Provider<SerializerEngine> serializerProvider
	) {
		this.messageService = messageService;
		this.messagesProvider = messagesProvider;
		this.serializerProvider = serializerProvider;
	}

	@Override
	public Component replaceTags(Component component, String tagFormat, Locale locale) {
		return replaceTags(component, tagFormat, locale, ComponentType.UNKNOWN);
	}

	@Override
	public Component replaceTags(Component component, String tagFormat, Locale locale, ComponentType source) {
		// Extract plain text from component
		String plainText = ComponentHelper.extractPlainText(component);

		// Find all tags in the text using TagParser
		List<TagParser.TagData> tags = TagParser.extractTags(plainText, tagFormat);

		if (tags.isEmpty()) return component;

		// Build replacement map - Component to Component (proper way!)
		Map<String, Component> replacements = new HashMap<>();

		for (TagParser.TagData tag : tags) {
			try {
				// Convert placeholders to Map<String, Object>
				Map<String, Object> placeholders = new HashMap<>();
				for (TagParser.TagData.Placeholder placeholder : tag.placeholders()) {
					placeholders.put(placeholder.name(), placeholder.value());
				}

				// Resolve the message
				String resolved = messageService.resolve(tag.key(), locale, placeholders);

				if (resolved != null && !resolved.equals(tag.key())) {
					replacements.put(tag.originalTag(), Component.text(resolved));
					continue;
				}

				// Resolution failed - use fallback Component with MiniMessage formatting
				Component fallbackComponent = formatFallbackComponent(tag.key(), locale, source);
				replacements.put(tag.originalTag(), fallbackComponent);

				// Log the missing translation
				logMissingTranslation(tag.key(), locale, source);
			} catch (Exception e) {
				// On exception, keep original tag as plain text
				replacements.put(tag.originalTag(), Component.text(tag.originalTag()));
			}
		}

		return ComponentHelper.replaceTextWithComponents(component, replacements);
	}

	@Override
	public boolean containsTags(Component component, String tagFormat) {
		String plainText = ComponentHelper.extractPlainText(component);
		return TagParser.containsTag(plainText, tagFormat);
	}

	/**
	 * Format a fallback message as a proper Component with MiniMessage formatting.
	 * This is the RIGHT way - no legacy codes, pure Component API!
	 *
	 * @param key    the message key that was not found
	 * @param locale the requested locale
	 * @param source the component type source
	 * @return formatted fallback Component with proper styling
	 */
	private Component formatFallbackComponent(String key, Locale locale, ComponentType source) {
		Messages messages = messagesProvider.get();
		if (messages == null || messages.getFallback() == null || !messages.getFallback().isEnabled())
			return Component.text(key);

		Messages.Fallback fallback = messages.getFallback();
		Messages.Fallback.SourceFormat format = getFormatForSource(fallback, source);

		if (format == null || !format.isEnabled())
			return Component.text(key);

		// Apply placeholder replacements
		String formatted = format.getFormat()
				.replace("{key}", key)
				.replace("{locale}", locale.toString())
				.replace("{source}", source.name());

		return serializerProvider.get().serialize(SerializerContent.builder()
				.message(formatted)
				.build());
	}

	/**
	 * Get the appropriate format configuration for the given source.
	 *
	 * @param fallback the fallback configuration
	 * @param source   the component type source
	 * @return the source format, or default format if none found
	 */
	private Messages.Fallback.SourceFormat getFormatForSource(Messages.Fallback fallback, ComponentType source) {
		if (fallback.getFormats() != null && fallback.getFormats().containsKey(source))
			return fallback.getFormats().get(source);

		return fallback.getDefaultFormat();
	}

	/**
	 * Log missing translation based on source configuration.
	 *
	 * @param key    the message key that was not found
	 * @param locale the requested locale
	 * @param source the component type source
	 */
	private void logMissingTranslation(String key, Locale locale, ComponentType source) {
		try {
			Messages messages = messagesProvider.get();
			if (messages == null || messages.getFallback() == null || !messages.getFallback().isEnabled())
				return;

			Messages.Fallback fallback = messages.getFallback();
			Messages.Fallback.SourceFormat format = getFormatForSource(fallback, source);

			if (format == null || !format.isLogMissing()) return;

			String logMessage = String.format("Missing translation for key '%s' (locale: %s, source: %s)",
					key, locale, source);

			Logger.warn(logMessage);
		} catch (Exception e) {
			// Silently ignore logging errors (e.g., Logger not initialized in tests)
		}
	}
}