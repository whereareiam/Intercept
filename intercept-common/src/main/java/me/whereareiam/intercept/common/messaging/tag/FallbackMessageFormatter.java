package me.whereareiam.intercept.common.messaging.tag;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;

import java.util.Locale;

@Singleton
public class FallbackMessageFormatter {
	private final Provider<Messages> messagesProvider;

	@Inject
	public FallbackMessageFormatter(Provider<Messages> messagesProvider) {
		this.messagesProvider = messagesProvider;
	}

	Component formatFallbackComponent(String key, Locale locale, ComponentType source) {
		Messages messages = messagesProvider.get();
		if (messages == null || messages.getFallback() == null || !messages.getFallback().isEnabled())
			return Component.text(key);

		Messages.Fallback fallback = messages.getFallback();
		Messages.Fallback.SourceFormat format = getFormatForSource(fallback, source);

		if (format == null || !format.isEnabled())
			return Component.text(key);

		String formatted = format.getFormat()
				.replace("{key}", key)
				.replace("{locale}", locale.toString())
				.replace("{source}", source.name());

		return Serializer.serialize(SerializerContent.builder()
				.message(formatted)
				.build());
	}

	void logMissingTranslation(String key, Locale locale, ComponentType source) {
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
		} catch (Exception ignored) {
			// Logger might not be initialized (tests), so ignore
		}
	}

	private Messages.Fallback.SourceFormat getFormatForSource(Messages.Fallback fallback, ComponentType source) {
		if (fallback.getFormats() != null && fallback.getFormats().containsKey(source))
			return fallback.getFormats().get(source);

		return fallback.getDefaultFormat();
	}
}