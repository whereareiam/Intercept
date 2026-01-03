package me.whereareiam.intercept.adapter.command.executor.locale;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@RequiredArgsConstructor
public abstract class AbstractLocaleCommand {
	protected final Provider<Messages> messagesProvider;

	@NotNull
	protected Messages.Commands.LocaleCommand getLocaleMessages() {
		Messages messages = messagesProvider.get();
		if (messages.getCommands() == null)
			messages.setCommands(new Messages.Commands());

		Messages.Commands.LocaleCommand locale = messages.getCommands().getLocale();
		if (locale == null) {
			locale = new Messages.Commands.LocaleCommand();
		}
		return locale;
	}

	@Nullable
	protected Locale parseLocale(@NotNull String localeInput) {
		try {
			return LocaleUtil.parseLocale(localeInput);
		} catch (Exception ignored) {
			return null;
		}
	}

	protected void sendPlainMessage(@NotNull Actor sender, @Nullable String template) {
		if (template == null || template.isBlank()) return;
		sender.sendMessage(Serializer.serialize(sender, template));
	}

	protected void sendLocaleError(
			@NotNull Actor sender,
			@Nullable String template,
			@NotNull String rawLocale
	) {
		if (template == null || template.isBlank()) return;

		SerializerContent.Builder builder = SerializerContent.builder()
				.receiver(sender)
				.message(template)
				.placeholder("locale", rawLocale);
		sender.sendMessage(Serializer.serialize(builder.build()));
	}

	protected void sendSuccess(
			@NotNull Actor sender,
			@Nullable String template,
			@NotNull Locale locale,
			@NotNull String playerName
	) {
		if (template == null || template.isBlank()) return;

		SerializerContent.Builder builder = SerializerContent.builder()
				.receiver(sender)
				.message(template)
				.placeholder("locale", LocaleUtil.formatLocale(locale))
				.placeholder("player", playerName);

		sender.sendMessage(Serializer.serialize(builder.build()));
	}
}

