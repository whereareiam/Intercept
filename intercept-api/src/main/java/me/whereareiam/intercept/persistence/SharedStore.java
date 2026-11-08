package me.whereareiam.intercept.persistence;

import me.whereareiam.intercept.model.shared.SharedChanges;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import org.jetbrains.annotations.NotNull;

/**
 * Stores what the network shares: messages changed from the organisation's text and rule files, at a revision that
 * goes up with every publication.
 */
public interface SharedStore {
	/**
	 * Reads everything at the current revision.
	 *
	 * @return the snapshot
	 */
	@NotNull SharedSnapshot load();

	/**
	 * Reads the current revision without the content, for polling.
	 *
	 * @return the revision, {@code 0} before anything was published
	 */
	long revision();

	/**
	 * Applies changes in one transaction and raises the revision.
	 *
	 * @param changes the changes; empty changes leave the revision as it is
	 * @return the revision afterwards
	 */
	long apply(@NotNull SharedChanges changes);
}
