package me.whereareiam.intercept.model.replication;

import lombok.Value;

/**
 * An administrator asked every instance to reload.
 */
@Value
public class ReloadRequested implements Replicated {
}
