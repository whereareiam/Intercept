package me.whereareiam.intercept.command;

import me.whereareiam.commandant.CommandMessageFormatter;
import me.whereareiam.keystone.model.Actor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * CommandMessageFormatter implementation for Intercept using MiniMessage.
 * Formats command exception messages by replacing {content} placeholder and parsing MiniMessage format.
 */
public class InterceptCommandMessageFormatter implements CommandMessageFormatter<Actor> {
	private final MiniMessage miniMessage;

	public InterceptCommandMessageFormatter() {
		this.miniMessage = MiniMessage.miniMessage();
	}

	@Override
	public @NotNull Component format(@NotNull Actor sender, @NotNull String message, @NotNull String content) {
		String formatted = message.replace("{content}", content);

		return miniMessage.deserialize(formatted);
	}
}