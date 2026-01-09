package me.whereareiam.intercept.common.tag;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.common.util.TagParser;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.intercept.common.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultTagReplacementService implements TagReplacementService {
	private final TagReplacementBuilder replacementBuilder;

	@Override
	@NotNull
	public Component replaceTags(@NotNull Component component, @NotNull String tagFormat, @NotNull Locale locale) {
		return replaceTags(component, tagFormat, locale, ComponentType.UNKNOWN);
	}

	@Override
	@NotNull
	public Component replaceTags(@NotNull Component component, @NotNull String tagFormat, @NotNull Locale locale, @NotNull ComponentType source) {
		String plainText = ComponentHelper.extractPlainText(component);

		List<TagParser.TagData> tags = TagParser.extractTags(plainText, tagFormat);
		if (tags.isEmpty()) return component;

		Map<String, Component> replacements = replacementBuilder.buildReplacements(tags, locale, source);
		return ComponentHelper.replaceTextWithComponents(component, replacements);
	}

	@Override
	public boolean containsTags(@NotNull Component component, @NotNull String tagFormat) {
		String plainText = ComponentHelper.extractPlainText(component);
		return TagParser.containsTag(plainText, tagFormat);
	}
}


