package me.whereareiam.intercept.common.messaging;

import com.google.inject.Singleton;
import me.whereareiam.intercept.messaging.TranslationData;
import me.whereareiam.intercept.messaging.TranslationLoader;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;

import java.util.Map;

/**
 * No-op loader for platforms that provide translations outside of Intercept files.
 */
@Singleton
public class NoopTranslationLoader implements TranslationLoader {
	@Override
	public TranslationData load() {
		return new MessageSnapshot(Map.of(), Map.of());
	}

	@Override
	public void resetStorage() {
		// No-op
	}
}
