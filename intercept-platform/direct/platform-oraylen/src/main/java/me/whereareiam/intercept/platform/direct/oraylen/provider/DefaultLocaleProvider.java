package me.whereareiam.intercept.platform.direct.oraylen.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.oraylen.api.model.config.Settings;

import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultLocaleProvider implements Provider<Locale> {
	private final Provider<Settings> settingsProvider;

	@Override
	public Locale get() {
		Settings settings = settingsProvider.get();
		if (settings != null && settings.getLocale() != null)
			return settings.getLocale();

		return Locale.ENGLISH;
	}
}
