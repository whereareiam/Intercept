package me.whereareiam.intercept.common.translation.loader;

import com.google.inject.Singleton;
import me.whereareiam.intercept.translation.TranslationData;
import me.whereareiam.intercept.translation.TranslationLoader;
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
