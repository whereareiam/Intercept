package me.whereareiam.intercept.replication;

import me.whereareiam.intercept.model.replication.Replicated;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Tells the other instances of the network what happened on this one. Without replication configured, nothing is
 * sent and instances notice publications by polling the revision.
 */
public interface ReplicationService {
	/**
	 * Sends a message to every other instance; this instance does not receive it.
	 *
	 * @param message the message
	 */
	void publish(@NotNull Replicated message);

	/**
	 * Calls a listener for each message of a type that another instance sends.
	 *
	 * @param type     the message type
	 * @param listener the listener
	 * @param <T>      the message type
	 */
	<T extends Replicated> void subscribe(@NotNull Class<T> type, @NotNull Consumer<T> listener);
}
