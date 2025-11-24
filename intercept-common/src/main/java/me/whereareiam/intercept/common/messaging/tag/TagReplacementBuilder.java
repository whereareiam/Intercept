package me.whereareiam.intercept.common.messaging.tag;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
public class TagReplacementBuilder {
	private final MessageService messageService;
	private final FallbackMessageFormatter fallbackFormatter;

	@Inject
	public TagReplacementBuilder(MessageService messageService, FallbackMessageFormatter fallbackFormatter) {
		this.messageService = messageService;
		this.fallbackFormatter = fallbackFormatter;
	}

	Map<String, Component> buildReplacements(List<TagParser.TagData> tags, Locale locale, ComponentType source) {
		Map<String, Component> replacements = new HashMap<>();

		for (TagParser.TagData tag : tags) {
			try {
				Map<String, Object> placeholders = convertPlaceholders(tag.placeholders());
				String resolved = messageService.resolve(tag.key(), locale, placeholders);

				if (resolved != null && !resolved.equals(tag.key())) {
					replacements.put(tag.originalTag(), Serializer.serialize(resolved));
					continue;
				}

				Component fallbackComponent = fallbackFormatter.formatFallbackComponent(tag.key(), locale, source);
				replacements.put(tag.originalTag(), fallbackComponent);
				fallbackFormatter.logMissingTranslation(tag.key(), locale, source);
			} catch (Exception e) {
				replacements.put(tag.originalTag(), Component.text(tag.originalTag()));
			}
		}

		return replacements;
	}

	private Map<String, Object> convertPlaceholders(List<TagParser.TagData.Placeholder> placeholders) {
		Map<String, Object> map = new HashMap<>();
		for (TagParser.TagData.Placeholder placeholder : placeholders)
			map.put(placeholder.name(), placeholder.value());

		return map;
	}
}