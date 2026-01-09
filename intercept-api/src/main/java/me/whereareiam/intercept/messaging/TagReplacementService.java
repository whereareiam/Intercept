package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Service for replacing translation tags in components.
 * <p>
 * Parses tags like {@code <lang key="..." param="...">} and replaces them
 * with translated content from the translation system.
 * <p>
 * Used by both interception platforms (automatic packet processing)
 * and direct platforms (automatic serializer processing via decorator).
 */
public interface TagReplacementService {
	/**
	 * Replace all translation tags in a component.
	 *
	 * @param component The component containing tags
	 * @param tagFormat The tag format to look for (e.g., "&lt;lang&gt;")
	 * @param locale    The target locale
	 * @return Component with tags replaced
	 */
	@NotNull
	Component replaceTags(@NotNull Component component, @NotNull String tagFormat, @NotNull Locale locale);

	/**
	 * Replace all translation tags in a component with source context.
	 *
	 * @param component The component containing tags
	 * @param tagFormat The tag format to look for (e.g., "&lt;lang&gt;")
	 * @param locale    The target locale
	 * @param source    The source context (for fallback formatting)
	 * @return Component with tags replaced
	 */
	@NotNull
	Component replaceTags(@NotNull Component component, @NotNull String tagFormat, @NotNull Locale locale, @NotNull ComponentType source);

	/**
	 * Check if a component contains translation tags.
	 *
	 * @param component The component to check
	 * @param tagFormat The tag format to look for (e.g., "&lt;lang&gt;")
	 * @return true if tags are present
	 */
	boolean containsTags(@NotNull Component component, @NotNull String tagFormat);
}
