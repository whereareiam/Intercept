package me.whereareiam.intercept.replication.transport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import org.jetbrains.annotations.NotNull;

/**
 * The contents of {@code replication.yml}.
 */
@Getter
@Setter
@ToString
public class Replication {
	@Comment("Whether to tell the other servers about publications, reloads and language choices at once. Without it they notice publications when they poll.")
	private boolean enabled;
	private @NotNull String host = "localhost";
	private int port = 6379;
	@ToString.Exclude
	@Comment("Empty when Redis needs no password.")
	private @NotNull String password = "";
	private boolean ssl;
	@Comment("The channel every server of one network uses; networks that share a Redis use different ones.")
	private @NotNull String channel = "intercept";
	@Comment("Milliseconds to wait for Redis.")
	private int timeout = 5_000;
}
