package me.whereareiam.intercept.command.executor.locale;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Command that allows players to update their preferred locale.
 */
@Singleton
public class LocaleCommand extends AbstractLocaleCommand {
	private final PlayerRegistry playerRegistry;

	@Inject
	public LocaleCommand(
			@NotNull Provider<Messages> messagesProvider,
			@NotNull PlayerRegistry playerRegistry
	) {
		super(messagesProvider);
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

		Locale locale = parseLocale(localeInput);
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
		if (!(sender instanceof InterceptPlayer interceptPlayer)) {
			sendPlainMessage(sender, localeMessages.getPlayerOnly());
			return null;
		}

		return interceptPlayer;
	}
}

