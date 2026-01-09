package me.whereareiam.intercept.platform.direct.oraylen.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.platform.direct.oraylen.SemanticaConfigurationFactory;
import me.whereareiam.semantica.SemanticaConfiguration;

import java.util.Locale;

public final class OraylenSemanticaConfigurationProvider implements Provider<SemanticaConfiguration<Locale>> {
	private final SemanticaConfigurationFactory factory;
	private final Settings interceptSettings;
	private final Provider<Locale> defaultLocaleProvider;

	@Inject
	public OraylenSemanticaConfigurationProvider(
			SemanticaConfigurationFactory factory,
			Settings interceptSettings,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider
	) {
		this.factory = factory;
		this.interceptSettings = interceptSettings;
		this.defaultLocaleProvider = defaultLocaleProvider;
	}

	@Override
	public SemanticaConfiguration<Locale> get() {
		Locale defaultLocale = defaultLocaleProvider == null ? null : defaultLocaleProvider.get();
		return factory.create(interceptSettings, defaultLocale == null ? Locale.ENGLISH : defaultLocale);
	}
}
