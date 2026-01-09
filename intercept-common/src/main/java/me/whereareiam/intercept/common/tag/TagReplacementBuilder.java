package me.whereareiam.intercept.common.tag;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.semantica.translation.TranslationService;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagReplacementBuilder {
	private final TranslationService<Locale> translationService;
	private final FallbackMessageFormatter fallbackFormatter;

	Map<String, Component> buildReplacements(List<TagParser.TagData> tags, Locale locale, ComponentType source) {
		Map<String, Component> replacements = new HashMap<>();

		for (TagParser.TagData tag : tags) {
			Map<String, Object> placeholders = convertPlaceholders(tag.placeholders());
			String resolved = translationService.resolve(tag.key(), locale, placeholders);

			if (!resolved.equals(tag.key())) {
				replacements.put(tag.originalTag(), Serializer.serialize(resolved));
				continue;
			}

			Component fallbackComponent = fallbackFormatter.formatFallbackComponent(tag.key(), locale, source);

			replacements.put(tag.originalTag(), fallbackComponent);
			try {
				fallbackFormatter.logMissingTranslation(tag.key(), locale, source);
			} catch (Exception ignored) {
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

