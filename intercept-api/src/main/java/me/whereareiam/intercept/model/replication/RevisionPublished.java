package me.whereareiam.intercept.model.replication;

import lombok.Value;

/**
 * An instance published a new revision of the shared messages and rules.
 */
@Value
public class RevisionPublished implements Replicated {
	long revision;
}
