package me.whereareiam.intercept.replication.transport;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Carries text messages between the instances of a network.
 */
public interface ReplicationTransport extends AutoCloseable {
	/**
	 * Sends a message to every instance subscribed to the channel, this one included.
	 *
	 * @param message the message
	 */
	void send(@NotNull String message);

	/**
	 * Receives the channel's messages until the transport is closed, reconnecting after a failure.
	 *
	 * @param receiver called on the transport's own thread for each message
	 */
	void receive(@NotNull Consumer<String> receiver);

	/**
	 * Stops receiving and closes the connection.
	 */
	@Override
	void close();
}
