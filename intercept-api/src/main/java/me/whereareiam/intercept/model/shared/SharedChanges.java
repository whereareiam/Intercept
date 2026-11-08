package me.whereareiam.intercept.model.shared;

import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Locale;

/**
 * The changes one publication makes to what the network shares.
 */
@Value
public class SharedChanges {
	/**
	 * Messages to add or replace.
	 */
	@NonNull List<SharedMessage> messages;
	/**
	 * Messages to remove.
	 */
	@NonNull List<MessageKey> removedMessages;
	/**
	 * Rule files to add or replace.
	 */
	@NonNull List<SharedRule> rules;
	/**
	 * Paths of rule files to remove.
	 */
	@NonNull List<String> removedRules;

	/**
	 * Tells whether there is nothing to change.
	 *
	 * @return whether the changes are empty
	 */
	public boolean isEmpty() {
		return messages.isEmpty() && removedMessages.isEmpty() && rules.isEmpty() && removedRules.isEmpty();
	}

	/**
	 * Identifies one message: a key in one language.
	 */
	@Value
	public static class MessageKey {
		@NonNull Locale language;
		@NonNull String key;
	}
}
