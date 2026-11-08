package me.whereareiam.intercept.interception.common;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.interception.MessageInterceptor;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.interception.common.marker.Marker;
import me.whereareiam.intercept.interception.common.marker.MarkerFormat;
import me.whereareiam.intercept.interception.common.rule.Rule;
import me.whereareiam.intercept.interception.common.rule.RuleFileReader;
import me.whereareiam.intercept.interception.config.Interception;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.translation.TranslationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * Replaces markers first; a message without markers is then tried against the rules, highest priority first, and the
 * first rule that matches translates it.
 */
@Singleton
public final class DefaultMessageInterceptor implements MessageInterceptor {
	private final TranslationService translations;
	private final Provider<Interception> interception;
	private final RuleFileReader reader;
	private final MessageService messages;
	private final Map<String, MarkerFormat> formats = new ConcurrentHashMap<>();

	private volatile List<Rule> rules;

	@Inject
	public DefaultMessageInterceptor(
			@NotNull TranslationService translations,
			@NotNull Provider<Interception> interception,
			@NotNull MessageService messages,
			@NotNull Configura configura,
			@Named("intercept") @NotNull Logger logger
	) {
		this.translations = translations;
		this.interception = interception;
		this.messages = messages;
		this.reader = new RuleFileReader(configura, logger);
		messages.onChange(() -> rules = null);
	}

	@Override
	public @Nullable Component intercept(@NotNull Surface surface, @NotNull Component message, @NotNull Locale language) {
		Interception.SurfaceSettings settings = interception.get().surface(surface);
		if (!settings.isEnabled()) return null;

		String plain = PlainTextComponentSerializer.plainText().serialize(message);
		MarkerFormat format = formats.computeIfAbsent(settings.getMarker(), MarkerFormat::of);
		if (format.pattern().matcher(plain).find()) return replaceMarkers(message, format, language);
		if (!settings.isRules()) return null;

		for (Rule rule : rules()) {
			if (!rule.surfaces().contains(surface)) continue;

			Matcher matcher = rule.pattern().matcher(plain);
			if (!matcher.find()) continue;

			if (rule.whole()) return translations.render(rule.key(), rule.arguments(matcher), language);
			return message.replaceText(TextReplacementConfig.builder()
					.match(rule.pattern())
					.replacement((result, builder) -> translations.render(rule.key(), arguments(rule, result.group()), language))
					.build());
		}

		return null;
	}

	private Component replaceMarkers(Component message, MarkerFormat format, Locale language) {
		return message.replaceText(TextReplacementConfig.builder()
				.match(format.pattern())
				.replacement((result, builder) -> {
					Matcher matcher = format.pattern().matcher(result.group());
					Marker marker = matcher.matches() ? Marker.parse(matcher.group(1)) : null;

					return marker == null ? builder : translations.render(marker.key(), marker.arguments(), language);
				})
				.build());
	}

	private static Map<String, String> arguments(Rule rule, String matched) {
		Matcher matcher = rule.pattern().matcher(matched);
		return matcher.find() ? rule.arguments(matcher) : Map.of();
	}

	private List<Rule> rules() {
		List<Rule> current = rules;
		if (current == null) {
			current = reader.read(messages.rules());
			rules = current;
		}

		return current;
	}
}
