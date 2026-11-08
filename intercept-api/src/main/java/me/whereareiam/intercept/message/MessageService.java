package me.whereareiam.intercept.message;

import me.whereareiam.intercept.model.message.PublishResult;
import me.whereareiam.intercept.model.message.RuleFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The messages and rules an administrator changes. Shared ones live in the database and in the {@code messages/} and
 * {@code rules/} folders of every server; local ones live in {@code local/} on one server.
 *
 * <p>Editing the shared folders changes nothing until {@link #publish(String)}; the other instances then reload the
 * shared layer when they see the new revision.</p>
 */
public interface MessageService {
	/**
	 * Sends the changes made in this server's {@code messages/} and {@code rules/} folders to the network. Changes
	 * that another server published in the meantime are kept; when both changed one key, this publication wins.
	 *
	 * @param author who published, shown with each change
	 * @return what changed
	 */
	@NotNull PublishResult publish(@NotNull String author);

	/**
	 * Rereads the local folders and the shared layer, and rewrites the shared folders from the database where they
	 * have no unpublished changes.
	 */
	void reload();

	/**
	 * Reloads the shared layer when another instance published a newer revision.
	 *
	 * @param revision the revision another instance announced, or {@code -1} to ask the database
	 */
	void refresh(long revision);

	/**
	 * Returns the revision of the shared layer this instance uses.
	 *
	 * @return the revision, {@code 0} before anything was published
	 */
	long revision();

	/**
	 * Returns the rule files in use: the shared ones followed by this server's local ones.
	 *
	 * @return the rule files, lowest precedence first
	 */
	@NotNull List<RuleFile> rules();

	/**
	 * Calls a listener whenever the messages or rules in use change.
	 *
	 * @param listener the listener
	 */
	void onChange(@NotNull Runnable listener);
}
