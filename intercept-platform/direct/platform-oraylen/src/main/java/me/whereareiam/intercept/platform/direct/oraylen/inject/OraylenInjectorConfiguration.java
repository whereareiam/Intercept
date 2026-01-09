package me.whereareiam.intercept.platform.direct.oraylen.inject;

import com.google.inject.*;
import com.google.inject.name.Names;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.adapter.command.resolver.CommandManagerResolver;
import me.whereareiam.intercept.adapter.command.resolver.ExternalCommandManagerResolver;
import me.whereareiam.intercept.common.provider.ReloadableProvider;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.platform.direct.oraylen.OraylenLoggingHelper;
import me.whereareiam.intercept.platform.direct.oraylen.provider.DefaultLocaleProvider;
import me.whereareiam.intercept.platform.direct.oraylen.config.provider.OraylenSettingsProvider;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.loader.OraylenTranslationLoader;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.OraylenTranslationRegistry;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.engine.OraylenTranslationEngine;
import me.whereareiam.intercept.platform.direct.oraylen.provider.OraylenSemanticaConfigurationProvider;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.intercept.registry.base.Registry;
import net.oraylen.api.translation.TranslationEngine;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

final class OraylenInjectorConfiguration extends AbstractModule {
	private final Path extensionPath;
	private final SerializerEngine serializerEngine;
	private final Provider<net.oraylen.api.model.config.Settings> settingsProvider;
	private final Injector platformInjector;
	private final Logger logger;

	OraylenInjectorConfiguration(
			Path extensionPath,
			SerializerEngine serializerEngine,
			Provider<net.oraylen.api.model.config.Settings> settingsProvider,
			Injector platformInjector,
			Logger logger
	) {
		this.extensionPath = extensionPath;
		this.serializerEngine = serializerEngine;
		this.settingsProvider = settingsProvider;
		this.platformInjector = platformInjector;
		this.logger = logger;
	}

	@Override
	protected void configure() {
		bind(Path.class).annotatedWith(Names.named("extensionPath")).toInstance(extensionPath);
		bind(SerializerEngine.class).toInstance(serializerEngine);
		bind(Logger.class).toInstance(logger);
		bind(net.oraylen.api.model.config.Settings.class).toProvider(settingsProvider);
		bind(Injector.class).annotatedWith(Names.named("platformInjector")).toInstance(platformInjector);
		bind(new TypeLiteral<CommandManagerResolver<Actor>>() {})
				.to(new TypeLiteral<ExternalCommandManagerResolver<Actor>>() {});
		bind(new TypeLiteral<Registry<Reloadable>>() {}).to(ReloadableProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Reloadable>>() {})
				.annotatedWith(Names.named("reloadables"))
				.toProvider(ReloadableProvider.class)
				.asEagerSingleton();
		bind(OraylenSettingsProvider.class).asEagerSingleton();
		bind(Settings.class).toProvider(OraylenSettingsProvider.class);
		bind(Locale.class)
				.annotatedWith(Names.named("defaultLocale"))
				.toProvider(DefaultLocaleProvider.class);
		bind(new TypeLiteral<SemanticaConfiguration<Locale>>() {})
				.toProvider(OraylenSemanticaConfigurationProvider.class)
				.asEagerSingleton();
		bind(OraylenTranslationEngine.class).asEagerSingleton();
		bind(TranslationEngine.class).to(OraylenTranslationEngine.class);
		bind(OraylenTranslationRegistry.class);
		bind(OraylenTranslationLoader.class);
		bind(LoggingHelper.class).to(OraylenLoggingHelper.class);
	}

	@Provides
	@Singleton
	TranslationService<Locale> provideTranslationService(
			SemanticaConfiguration<Locale> configuration,
			OraylenTranslationRegistry registry
	) {
		return Semantica.createService(configuration, registry);
	}
}
