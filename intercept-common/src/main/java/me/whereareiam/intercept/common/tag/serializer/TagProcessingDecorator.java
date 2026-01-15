package me.whereareiam.intercept.common.tag.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.keystone.model.SerializerContent;
import me.whereareiam.keystone.serializer.MessageDecorator;
import me.whereareiam.semantica.translation.TranslationService;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MessageDecorator that processes translation tags in messages.
 * <p>
 * Automatically replaces {@code <lang key="..." param="...">} tags with
 * translated content during the serialization pipeline.
 * <p>
 * This runs BEFORE placeholder replacement and adapter deserialization,
 * operating at the string level to avoid circular dependencies.
 * <p>
 * Configuration:
 * - {@code settings.translation.tag.auto-process}: Enable/disable automatic processing
 * - {@code settings.translation.tag.format}: Tag format to use (e.g., "{@code <lang>}")
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagProcessingDecorator implements MessageDecorator {
	private final TranslationService<Locale> translationService;
	private final Provider<Settings> settingsProvider;

	@NotNull
	@Override
	public SerializerContent decorate(@NotNull SerializerContent content) {
		Settings settings = settingsProvider.get();

		Settings.Translation translation = settings.getTranslation();
		Settings.Translation.Tag tag = translation != null ? translation.getTag() : null;

		// Check if auto-processing is enabled
		if (tag == null || !tag.isAutoProcess())
			return content;

		String tagFormat = tag.getFormat();
		if (tagFormat == null || tagFormat.isEmpty())
			return content;

		// Get locale from receiver (Actor)
		Locale locale = content.getReceiver() != null
				? content.getReceiver().getLocale()
				: Locale.ENGLISH;

		String message = content.getMessage();

		// Quick check before expensive operations - look for tag name
		String tagName = extractTagName(tagFormat);
		if (!message.contains(tagName))
			return content;

		// Extract and replace tags at string level
		List<TagParser.TagData> tags = TagParser.extractTags(message, tagFormat);
		if (tags.isEmpty()) {
			return content;
		}

		// Build replacements map
		Map<String, String> replacements = new HashMap<>();
		for (TagParser.TagData tagData : tags) {
			try {
				Map<String, Object> placeholders = convertPlaceholders(tagData.placeholders());
				String resolved = translationService.resolve(tagData.key(), locale, placeholders);

				if (resolved != null && !resolved.equals(tagData.key())) {
					replacements.put(tagData.originalTag(), resolved);
					continue;
				}

				// Fallback: just use the key
				replacements.put(tagData.originalTag(), tagData.key());
			} catch (Exception e) {
				// On error, leave the tag as-is (will be visible to help debug)
				replacements.put(tagData.originalTag(), tagData.originalTag());
			}
		}

		// Apply replacements to message string
		String processedMessage = message;
		for (Map.Entry<String, String> entry : replacements.entrySet())
			processedMessage = processedMessage.replace(entry.getKey(), entry.getValue());

		// Return new content with processed message
		return SerializerContent.builder()
				.receiver(content.getReceiver())
				.scope(content.getScope())
				.message(processedMessage)
				.placeholders(content.getPlaceholders())
				.build();
	}

	@Override
	public boolean isAvailable() {
		Settings settings = settingsProvider.get();
		Settings.Translation translation = settings.getTranslation();
		Settings.Translation.Tag tag = translation != null ? translation.getTag() : null;
		return tag != null
				&& tag.isAutoProcess()
				&& tag.getFormat() != null
				&& !tag.getFormat().isEmpty();
	}

	/**
	 * Extract tag name from format by removing first and last character.
	 * Examples:
	 * - "{@code <lang>}" → "lang"
	 * - "{@code [tr]}" → "tr"
	 * - "{@code {i18n}}" → "i18n"
	 * - "{@code <translation>}" → "translation"
	 */
	private String extractTagName(String tagFormat) {
		if (tagFormat == null || tagFormat.length() < 3)
			return tagFormat;

		return tagFormat.substring(1, tagFormat.length() - 1);
	}

	/**
	 * Convert tag placeholders to translation service format.
	 */
	@NotNull
	private Map<String, Object> convertPlaceholders(@NotNull List<TagParser.TagData.Placeholder> placeholders) {
		Map<String, Object> map = new HashMap<>();
		for (TagParser.TagData.Placeholder placeholder : placeholders)
			map.put(placeholder.name(), placeholder.value());

		return map;
	}
}
