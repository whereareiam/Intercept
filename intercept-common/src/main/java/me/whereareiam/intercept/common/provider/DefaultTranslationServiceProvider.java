package me.whereareiam.intercept.common.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.translation.TranslationRegistry;
import me.whereareiam.semantica.translation.TranslationService;

import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class DefaultTranslationServiceProvider implements Provider<TranslationService<Locale>> {
	private final SemanticaConfiguration<Locale> configuration;
	private final TranslationRegistry registry;

	@Override
	public TranslationService<Locale> get() {
		return Semantica.createService(configuration, registry);
	}
}
