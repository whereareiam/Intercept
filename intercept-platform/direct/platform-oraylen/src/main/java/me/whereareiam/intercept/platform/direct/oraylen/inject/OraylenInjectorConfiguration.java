package me.whereareiam.intercept.platform.direct.oraylen.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import me.whereareiam.intercept.PlatformInteractor;
import me.whereareiam.intercept.Scheduler;
import me.whereareiam.intercept.common.persistence.format.type.locale.LocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.multilocale.MultiLocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.template.TemplateFormat;
import me.whereareiam.intercept.common.translation.loader.DefaultTranslationLoader;
import me.whereareiam.intercept.translation.mapper.PlaceholderMapper;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.translation.TranslationLoader;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.platform.direct.oraylen.OraylenLoggingHelper;
import me.whereareiam.intercept.platform.direct.oraylen.OraylenPlatformInteractor;
import me.whereareiam.intercept.platform.direct.oraylen.config.PlatformSettings;
import me.whereareiam.intercept.platform.direct.oraylen.config.provider.OraylenSettingsProvider;
import me.whereareiam.intercept.platform.direct.oraylen.config.provider.PlatformSettingsProvider;
import me.whereareiam.intercept.platform.direct.oraylen.listener.OraylenListenerRegistrar;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenNamespaceProvider;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenTranslationRegistry;
import me.whereareiam.intercept.platform.direct.oraylen.translation.loader.mapper.OraylenTranslationEntryMapper;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenTranslationEngine;
import me.whereareiam.intercept.platform.direct.oraylen.translation.mapper.OraylenPlaceholderMapper;
import me.whereareiam.intercept.platform.direct.oraylen.translation.loader.OraylenTranslationLoader;
import me.whereareiam.intercept.platform.direct.oraylen.provider.DefaultLocaleProvider;
import me.whereareiam.intercept.platform.direct.oraylen.OraylenScheduler;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.semantica.translation.TranslationRegistry;
import net.oraylen.api.translation.TranslationEngine;
import net.oraylen.api.translation.Placeholder;
import org.incendo.cloud.CommandManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

final class OraylenInjectorConfiguration extends AbstractModule {
	private final Path extensionPath;
	private final SerializerEngine serializerEngine;
	private final Provider<net.oraylen.api.model.config.Settings> settingsProvider;
	private final Logger logger;
	private final CommandManager<Actor> platformCommandManager;

	public OraylenInjectorConfiguration(
			Path extensionPath,
			SerializerEngine serializerEngine,
			Provider<net.oraylen.api.model.config.Settings> settingsProvider,
			Logger logger,
			CommandManager<Actor> platformCommandManager
	) {
		this.extensionPath = extensionPath;
		this.serializerEngine = serializerEngine;
		this.settingsProvider = settingsProvider;
		this.logger = logger;
		this.platformCommandManager = platformCommandManager;
	}

	@Override
	protected void configure() {
		requestInjection(this);

		bind(Path.class).annotatedWith(Names.named("extensionPath")).toInstance(extensionPath);
		bind(SerializerEngine.class).toInstance(serializerEngine);
		bind(Logger.class).toInstance(logger);
		bind(net.oraylen.api.model.config.Settings.class).toProvider(settingsProvider);
		bind(new TypeLiteral<CommandManager<Actor>>() {}).toInstance(platformCommandManager);

		bind(OraylenSettingsProvider.class).in(Singleton.class);
		OptionalBinder.newOptionalBinder(binder(), Settings.class)
				.setBinding().toProvider(OraylenSettingsProvider.class).in(Singleton.class);

		bind(PlatformSettingsProvider.class).asEagerSingleton();
		bind(PlatformSettings.class).toProvider(PlatformSettingsProvider.class).in(Singleton.class);

		OptionalBinder.newOptionalBinder(
				binder(),
				Key.get(Locale.class, Names.named("defaultLocale"))
		).setBinding().toProvider(DefaultLocaleProvider.class);

		bind(OraylenTranslationEngine.class).asEagerSingleton();
		bind(TranslationEngine.class).to(OraylenTranslationEngine.class);
		bind(OraylenTranslationRegistry.class).in(Singleton.class);
		OptionalBinder.newOptionalBinder(binder(), TranslationRegistry.class)
				.setBinding().to(OraylenTranslationRegistry.class).in(Singleton.class);
		bind(OraylenTranslationLoader.class);
		bind(OraylenTranslationEntryMapper.class);
		bind(new TypeLiteral<PlaceholderMapper<Map<String, Placeholder>>>() {})
				.to(OraylenPlaceholderMapper.class);

		bind(ListenerRegistrar.class).to(OraylenListenerRegistrar.class).asEagerSingleton();
		bind(PlatformInteractor.class).to(OraylenPlatformInteractor.class).asEagerSingleton();
		bind(Scheduler.class).to(OraylenScheduler.class).asEagerSingleton();
		bind(LoggingHelper.class).to(OraylenLoggingHelper.class);

		OptionalBinder.newOptionalBinder(binder(), PlatformNamespaceProvider.class)
				.setBinding().to(OraylenNamespaceProvider.class);
		OptionalBinder.newOptionalBinder(binder(), TranslationLoader.class)
				.setBinding().to(DefaultTranslationLoader.class);
	}

	@Inject
	void registerFormats(MessageFormatRegistry formatRegistry) {
		if (formatRegistry == null) return;
		formatRegistry.register(new LocaleFormat(), true);
		formatRegistry.register(new MultiLocaleFormat(), false);
		formatRegistry.register(new TemplateFormat(), false);
	}
}
