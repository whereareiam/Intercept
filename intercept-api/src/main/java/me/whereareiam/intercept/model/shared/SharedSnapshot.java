package me.whereareiam.intercept.model.shared;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Everything the network shares at one revision.
 */
@Value
public class SharedSnapshot {
	public static final SharedSnapshot EMPTY = new SharedSnapshot(0, List.of(), List.of());

	long revision;
	@NonNull List<SharedMessage> messages;
	@NonNull List<SharedRule> rules;
}
