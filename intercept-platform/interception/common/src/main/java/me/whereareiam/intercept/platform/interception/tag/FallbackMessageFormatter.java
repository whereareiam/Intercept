package me.whereareiam.intercept.platform.interception.tag;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.type.ComponentType;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FallbackMessageFormatter {
	private final Provider<Messages> messagesProvider;

	public Component formatFallbackComponent(String key, Locale locale, ComponentType source) {
		String formatted = formatFallbackText(key, locale, source);
		if (formatted == null) return Component.text(key);

		return Serializer.serialize(formatted);
	}

	public void logMissingTranslation(String key, Locale locale, ComponentType source) {
		Messages.Fallback.SourceFormat format = resolveFormat(source);
		if (format == null || !format.isLogMissing()) return;

		Logger.warn("Missing translation for key '%s' (locale=%s, source=%s)",
				key,
				locale != null ? locale.toString() : "unknown",
				source != null ? source.name() : ComponentType.UNKNOWN.name());
	}

	private String formatFallbackText(String key, Locale locale, ComponentType source) {
		Messages.Fallback.SourceFormat format = resolveFormat(source);
		if (format == null || !format.isEnabled()) return null;

		String template = format.getFormat();
		if (template == null || template.isBlank()) return null;

		String localeValue = locale != null ? locale.toString() : "unknown";
		String sourceValue = source != null ? source.name() : ComponentType.UNKNOWN.name();

		return template
				.replace("<key>", key)
				.replace("<locale>", localeValue)
				.replace("<source>", sourceValue);
	}

	private Messages.Fallback.SourceFormat resolveFormat(ComponentType source) {
		Messages messages = messagesProvider.get();
		if (messages == null) return null;

		Messages.Fallback fallback = messages.getFallback();
		if (fallback == null || !fallback.isEnabled()) return null;

		Map<String, Messages.Fallback.SourceFormat> formats = fallback.getFormats();
		if (source == null) source = ComponentType.UNKNOWN;

		Messages.Fallback.SourceFormat format = null;
		if (formats != null)
			format = formats.get(source.name());

		if (format != null && format.isEnabled())
			return format;

		Messages.Fallback.SourceFormat defaultFormat = fallback.getDefaultFormat();
		if (defaultFormat != null && defaultFormat.isEnabled())
			return defaultFormat;

		return null;
	}
}
