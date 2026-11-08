package me.whereareiam.intercept.adapter.command.executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.adapter.command.Replies;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.model.message.PublishResult;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code /intercept publish} sends the changes in {@code messages/} and {@code rules/} to every server.
 */
@Singleton
public final class PublishCommand {
	private final MessageService messages;
	private final Replies replies;
	private final Logger logger;

	@Inject
	public PublishCommand(@NotNull MessageService messages, @NotNull Replies replies, @Named("intercept") @NotNull Logger logger) {
		this.messages = messages;
		this.replies = replies;
		this.logger = logger;
	}

	/**
	 * Adds the command's handler.
	 *
	 * @param builder the command
	 * @return the finished builder
	 */
	public @NotNull Command.Builder<CommandSource> build(@NotNull Command.Builder<CommandSource> builder) {
		return builder.handler(this::execute);
	}

	private void execute(CommandContext<CommandSource> context) {
		CommandSource source = context.sender();
		PublishResult result;
		try {
			result = messages.publish(source.name());
		} catch (RuntimeException failure) {
			logger.log(Level.WARNING, "Cannot publish", failure);
			replies.send(source, "intercept.publish.failed", Map.of("reason", String.valueOf(failure.getMessage())));
			return;
		}

		if (result.isEmpty()) {
			replies.send(source, "intercept.publish.nothing", Map.of());
			return;
		}

		replies.send(source, "intercept.publish.done", Map.of(
				"revision", result.getRevision(),
				"changed", result.getChangedMessages(),
				"removed", result.getRemovedMessages(),
				"rules", result.getChangedRules()
		));
	}
}
