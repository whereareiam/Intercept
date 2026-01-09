package me.whereareiam.intercept.platform.interception.tag;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.intercept.platform.interception.util.ComponentHelper;
import me.whereareiam.intercept.platform.interception.util.TagParser;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagReplacementService {
	private final TagReplacementBuilder replacementBuilder;

	public Component replaceTags(Component component, String tagFormat, Locale locale) {
		return replaceTags(component, tagFormat, locale, ComponentType.UNKNOWN);
	}

	public Component replaceTags(Component component, String tagFormat, Locale locale, ComponentType source) {
		String plainText = ComponentHelper.extractPlainText(component);

		List<TagParser.TagData> tags = TagParser.extractTags(plainText, tagFormat);
		if (tags.isEmpty()) return component;

		Map<String, Component> replacements = replacementBuilder.buildReplacements(tags, locale, source);
		return ComponentHelper.replaceTextWithComponents(component, replacements);
	}

	public boolean containsTags(Component component, String tagFormat) {
		String plainText = ComponentHelper.extractPlainText(component);
		return TagParser.containsTag(plainText, tagFormat);
	}
}

