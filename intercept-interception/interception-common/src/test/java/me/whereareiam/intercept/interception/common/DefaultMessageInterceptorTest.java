package me.whereareiam.intercept.interception.common;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.interception.Surface;
import me.whereareiam.intercept.interception.config.Interception;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.model.message.PublishResult;
import me.whereareiam.intercept.model.message.RuleFile;
import me.whereareiam.intercept.translation.TranslationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultMessageInterceptorTest {
	private static final String RULES = """
			no-permission:
			  pattern: "You don't have permission: ([\\\\w.]+)"
			  key: essentials.permission.missing
			  arguments:
			    permission: 1
			""";

	private final Interception interception = new Interception();
	private final DefaultMessageInterceptor interceptor = new DefaultMessageInterceptor(
			new EchoTranslations(),
			() -> interception,
			new Rules(List.of(new RuleFile("essentials.yml", RULES, false))),
			Configura.builder().format(Format.YAML).build(),
			Logger.getAnonymousLogger()
	);

	@Test
	void aMarkerBecomesItsKeyInTheReceiversLanguage() {
		Component result = interceptor.intercept(
				Surface.CHAT,
				Component.text("» <lang key=essentials.motd player=Alice> «"),
				Locale.GERMAN
		);

		assertEquals("» [de essentials.motd player=Alice] «", plain(result));
	}

	@Test
	void aMatchingRuleReplacesTheMatchedPart() {
		Component result = interceptor.intercept(
				Surface.CHAT,
				Component.text("Error: You don't have permission: essentials.fly"),
				Locale.ENGLISH
		);

		assertEquals("Error: [en essentials.permission.missing permission=essentials.fly]", plain(result));
	}

	@Test
	void aMessageWithoutMarkersOrRulesAndATurnedOffSurfaceAreLeftAlone() {
		assertNull(interceptor.intercept(Surface.CHAT, Component.text("Hello"), Locale.ENGLISH));

		interception.of(Surface.KICK).setEnabled(false);
		assertNull(interceptor.intercept(Surface.KICK, Component.text("<lang key=a.b>"), Locale.ENGLISH));
	}

	@Test
	void rulesApplyOnlyToTheirSurfaces() {
		assertNull(interceptor.intercept(Surface.KICK, Component.text("You don't have permission: x"), Locale.ENGLISH));
	}

	private static String plain(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	/**
	 * Renders a key as its language, the key and its arguments, so a test sees what was asked for.
	 */
	private static final class EchoTranslations implements TranslationService {
		@Override
		public boolean has(@NotNull String key) {
			return true;
		}

		@Override
		public @NotNull Component render(@NotNull String key, @NotNull Map<String, ?> arguments, @NotNull Locale language) {
			StringBuilder text = new StringBuilder("[" + language.toLanguageTag() + " " + key);
			arguments.forEach((name, value) -> text.append(' ').append(name).append('=').append(value));

			return Component.text(text.append(']').toString());
		}

		@Override
		public void reload() {
		}
	}

	private record Rules(List<RuleFile> files) implements MessageService {
		@Override
		public @NotNull PublishResult publish(@NotNull String author) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reload() {
		}

		@Override
		public void refresh(long revision) {
		}

		@Override
		public long revision() {
			return 0;
		}

		@Override
		public @NotNull List<RuleFile> rules() {
			return files;
		}

		@Override
		public void onChange(@NotNull Runnable listener) {
		}
	}
}
