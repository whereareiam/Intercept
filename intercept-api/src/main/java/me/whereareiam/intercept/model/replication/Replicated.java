package me.whereareiam.intercept.model.replication;

/**
 * A message one instance sends to the others.
 */
public sealed interface Replicated permits LanguageChosen, ReloadRequested, RevisionPublished {
}
