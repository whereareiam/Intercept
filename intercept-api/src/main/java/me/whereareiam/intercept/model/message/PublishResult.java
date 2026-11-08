package me.whereareiam.intercept.model.message;

import lombok.Value;

/**
 * What a publication changed.
 */
@Value
public class PublishResult {
	/**
	 * The revision the network uses afterwards; the previous one when nothing changed.
	 */
	long revision;
	/**
	 * Messages added or changed.
	 */
	int changedMessages;
	/**
	 * Messages removed, including those set back to the remote text.
	 */
	int removedMessages;
	/**
	 * Rule files added, changed or removed.
	 */
	int changedRules;

	/**
	 * Tells whether the publication changed nothing.
	 *
	 * @return whether nothing changed
	 */
	public boolean isEmpty() {
		return changedMessages == 0 && removedMessages == 0 && changedRules == 0;
	}
}
