package me.whereareiam.intercept.adapter.command.executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.adapter.command.Replies;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.model.replication.ReloadRequested;
import me.whereareiam.intercept.replication.ReplicationService;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * {@code /intercept reload} rereads this server's messages and rules; with {@code --everywhere} every other server
 * reloads too.
 */
@Singleton
public final class ReloadCommand {
	private static final String EVERYWHERE = "everywhere";

	private final MessageService messages;
	private final ReplicationService replication;
	private final Replies replies;

	@Inject
	public ReloadCommand(@NotNull MessageService messages, @NotNull ReplicationService replication, @NotNull Replies replies) {
		this.messages = messages;
		this.replication = replication;
		this.replies = replies;
	}

	/**
	 * Adds the command's flag and handler.
	 *
	 * @param builder the command
	 * @return the finished builder
	 */
	public @NotNull Command.Builder<CommandSource> build(@NotNull Command.Builder<CommandSource> builder) {
		return builder
				.flag(CommandFlag.builder(EVERYWHERE))
				.handler(this::execute);
	}

	private void execute(CommandContext<CommandSource> context) {
		messages.reload();
		if (!context.flags().isPresent(EVERYWHERE)) {
			replies.send(context.sender(), "intercept.reload.done", Map.of());
			return;
		}

		replication.publish(new ReloadRequested());
		replies.send(context.sender(), "intercept.reload.everywhere", Map.of());
	}
}
