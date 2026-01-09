package me.whereareiam.intercept.adapter.command.executor.locale;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import me.whereareiam.semantica.translation.TranslationService;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Command that allows administrators to update another player's preferred locale.
 */
@Singleton
public class LocaleTargetCommand extends AbstractLocaleCommand {
	private final PlayerRegistry playerRegistry;

	@Inject
	public LocaleTargetCommand(
			@NotNull Provider<Messages> messagesProvider,
			@NotNull PlayerRegistry playerRegistry,
			@NotNull TranslationService<Locale> translationService
	) {
		super(messagesProvider, translationService);
		this.playerRegistry = playerRegistry;
	}

	@Definition("locale-target")
	@Command("intercept locale <player> <locale>")
	public void command(
			@NotNull Actor sender,
			@Argument(value = "player", suggestions = "players") @NotNull String targetName,
			@Argument(value = "locale", suggestions = "locales") @NotNull String localeInput
	) {
		Messages.Commands.LocaleCommand localeMessages = getLocaleMessages();

		Optional<InterceptPlayer> targetOptional = playerRegistry.getPlayerData(targetName);
		if (targetOptional.isEmpty()) {
			sendPlayerNotFound(sender, localeMessages.getPlayerNotFound(), targetName);
			return;
		}

		Locale locale = parseAndValidateLocale(localeInput);
		if (locale == null) {
			sendLocaleError(sender, localeMessages.getInvalidLocale(), localeInput);
			return;
		}

		InterceptPlayer target = targetOptional.get();
		target.setLocale(locale);
		playerRegistry.syncPlayerData(target);

		sendSuccess(sender, localeMessages.getTargetUpdated(), locale, target.getUsername());
	}

	private void sendPlayerNotFound(@NotNull Actor sender, String template, String targetName) {
		if (template == null || template.isBlank()) return;

		SerializerContent.Builder builder = SerializerContent.builder()
				.receiver(sender)
				.message(template)
				.placeholder("player", targetName);

		sender.sendMessage(Serializer.serialize(builder.build()));
	}
}

