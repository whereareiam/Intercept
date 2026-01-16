package me.whereareiam.intercept.adapter.command.executor.locale;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.commandant.model.Console;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.keystone.Actor;
import me.whereareiam.semantica.translation.TranslationService;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Command that allows players to update their preferred locale.
 */
@Singleton
public class LocaleCommand extends AbstractLocaleCommand {
	private final PlayerRegistry playerRegistry;

	@Inject
	public LocaleCommand(
			@NotNull Provider<Messages> messagesProvider,
			@NotNull PlayerRegistry playerRegistry,
			@NotNull TranslationService<Locale> translationService
	) {
		super(messagesProvider, translationService);
		this.playerRegistry = playerRegistry;
	}

	@Definition("locale-self")
	@Command("locale <locale>")
	public void command(
			@NotNull Actor sender,
			@Argument(value = "locale", suggestions = "locales") @NotNull String localeInput
	) {
		Messages.Commands.LocaleCommand localeMessages = getLocaleMessages();

		InterceptPlayer interceptPlayer = validateSenderIsPlayer(sender, localeMessages);
		if (interceptPlayer == null) return;

		Locale locale = parseAndValidateLocale(localeInput);
		if (locale == null) {
			sendLocaleError(sender, localeMessages.getInvalidLocale(), localeInput);
			return;
		}

		interceptPlayer.setLocale(locale);
		playerRegistry.syncPlayerData(interceptPlayer);

		sendSuccess(sender, localeMessages.getSelfUpdated(), locale, interceptPlayer.getUsername());
	}

	@Nullable
	private InterceptPlayer validateSenderIsPlayer(
			@NotNull Actor sender,
			@NotNull Messages.Commands.LocaleCommand localeMessages
	) {
		if (sender instanceof Console) {
			sendPlainMessage(sender, localeMessages.getPlayerOnly());
			return null;
		}

		if (sender instanceof InterceptPlayer interceptPlayer) return interceptPlayer;

		Optional<InterceptPlayer> stored = playerRegistry.getPlayerData(sender.getUniqueId());
		if (stored.isEmpty()) {
			sendPlainMessage(sender, localeMessages.getPlayerOnly());
			return null;
		}

		return stored.get();
	}
}

