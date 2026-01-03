package me.whereareiam.intercept.common;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.common.config.ConfiguraBootstrap;
import me.whereareiam.intercept.common.config.resolver.FileSystemConfigurationTypeResolver;
import me.whereareiam.intercept.common.event.EventController;
import me.whereareiam.intercept.common.logging.BannerContributor;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.InterceptSemanticaLogger;
import me.whereareiam.intercept.common.messaging.MessageLifecycleHook;
import me.whereareiam.intercept.common.messaging.MessageLifecycleService;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageDataService;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileWriter;
import me.whereareiam.intercept.common.messaging.persistence.MessageDocumentProcessor;
import me.whereareiam.intercept.common.player.DefaultPlayerRegistry;
import me.whereareiam.intercept.common.provider.IntegrationProvider;
import me.whereareiam.intercept.common.provider.ReloadableProvider;
import me.whereareiam.intercept.common.provider.SerializerEngineProvider;
import me.whereareiam.intercept.common.provider.config.CommandsProvider;
import me.whereareiam.intercept.common.provider.config.MessagesProvider;
import me.whereareiam.intercept.common.provider.config.PersistenceProvider;
import me.whereareiam.intercept.common.provider.config.SettingsProvider;
import me.whereareiam.intercept.common.updater.provider.GitHubProvider;
import me.whereareiam.intercept.common.updater.provider.ModrinthProvider;
import me.whereareiam.intercept.common.updater.provider.SpigotMCProvider;
import me.whereareiam.intercept.config.ConfigurationTypeResolver;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.integration.Integration;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.ProviderType;
import me.whereareiam.intercept.updater.UpdateProvider;
import me.whereareiam.intercept.util.EventUtil;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.SemanticaLogger;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.intercept.common.messaging.InterceptTranslationRegistry;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.translation.TranslationRegistry;
import me.whereareiam.semantica.translation.TranslationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
public class CommonConfiguration extends AbstractModule {
	private final Path dataPath;

	public CommonConfiguration(Path dataPath) {
		this.dataPath = dataPath;
	}

	@Override
	protected void configure() {
		requestInjection(this);

		// Configuration
		bind(ConfigurationTypeResolver.class)
				.to(FileSystemConfigurationTypeResolver.class)
				.asEagerSingleton();
		bind(ConfiguraBootstrap.class).asEagerSingleton();

		// Configs
		bind(SettingsProvider.class).asEagerSingleton();
		bind(Settings.class).toProvider(SettingsProvider.class);
		bind(PersistenceProvider.class).asEagerSingleton();
		bind(Persistence.class).toProvider(PersistenceProvider.class);
		bind(MessagesProvider.class).asEagerSingleton();
		bind(Messages.class).toProvider(MessagesProvider.class);
		bind(CommandsProvider.class).asEagerSingleton();
		bind(Commands.class).toProvider(CommandsProvider.class);

		// Services
		bind(SerializerEngine.class).toProvider(SerializerEngineProvider.class);
		bind(EventManager.class).to(EventController.class);
		bind(EventUtil.class).asEagerSingleton();
		bind(PlayerRegistry.class).to(DefaultPlayerRegistry.class);

		// Plugin
		bind(Intercept.class).asEagerSingleton();

		// Messages system
		bind(MessageRegistry.class).to(DefaultMessageRegistry.class);
		bind(MessageFileLoader.class).to(DefaultMessageFileLoader.class);
		bind(MessageFileWriter.class).to(DefaultMessageFileWriter.class);
		bind(MessageDataService.class).to(DefaultMessageDataService.class);
		bind(MessageLifecycleService.class).asEagerSingleton();

		// Updater
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.MODRINTH.toString()))
				.to(ModrinthProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.GITHUB.toString()))
				.to(GitHubProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.SPIGOT.toString()))
				.to(SpigotMCProvider.class);

		// Other
		bind(new TypeLiteral<Registry<Integration>>() {
		}).to(IntegrationProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Integration>>() {
		}).toProvider(IntegrationProvider.class).asEagerSingleton();

		bind(new TypeLiteral<Registry<Reloadable>>() {
		}).to(ReloadableProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Reloadable>>() {
		}).annotatedWith(Names.named("reloadables")).toProvider(ReloadableProvider.class).asEagerSingleton();

		Multibinder.newSetBinder(binder(), BannerContributor.class);
		Multibinder.newSetBinder(binder(), MessageLifecycleHook.class);
		Multibinder.newSetBinder(binder(), MessageDocumentProcessor.class);
	}

	@Inject
	void initializeSerializationHelper(Provider<SerializerEngine> serializerProvider) {
		Serializer.initialize(serializerProvider);
	}

	@Inject
	void initializeEventUtil(EventManager eventManager) {
		EventUtil.initialize(eventManager);
	}

	@Provides
	@Singleton
	Path provideBasePath() {
		return ensureDirectory(dataPath, "data");
	}

	@Provides
	@Singleton
	@Named("dataPath")
	Path provideNamedDataPath() {
		return ensureDirectory(dataPath, "data");
	}

	@Provides
	@Singleton
	@Named("messagesPath")
	Path provideMessagesPath(@Named("dataPath") Path dataPath) {
		return ensureDirectory(dataPath.resolve("messages"), "messages");
	}

	@Provides
	@Singleton
	SemanticaLogger provideSemanticaLogger() {
		return new InterceptSemanticaLogger();
	}

	@Provides
	@Singleton
	LocaleParser<Locale> provideLocaleParser() {
		return SemanticLocale::wrap;
	}

	@Provides
	@Singleton
	SemanticaConfiguration<Locale> provideSemanticaConfiguration(
			Provider<Settings> settingsProvider,
			LocaleParser<Locale> localeParser,
			SemanticaLogger logger
	) {
		Settings settings = settingsProvider.get();
		Settings.Performance.Cache cache = settings.getPerformance().getCache();

		return SemanticaConfiguration.<Locale>builder()
				.defaultLocale(SemanticLocale.wrap(settings.getLocale()))
				.tagConfiguration(TagConfiguration.defaults())
				.performance(SemanticaConfiguration.PerformanceSettings.builder()
						.cache(SemanticaConfiguration.PerformanceSettings.CacheSettings.builder()
								.enabled(cache.isEnabled())
								.semiStaticSize(cache.getSemiStaticSize())
								.dynamicSize(cache.getDynamicSize())
								.semiStaticExpireMinutes(cache.getSemiStaticExpireMinutes())
								.dynamicExpireMinutes(cache.getDynamicExpireMinutes())
								.build())
						.prerenderStatic(settings.getPerformance().isPrerenderStatic())
						.buildDependencyGraph(settings.getPerformance().isBuildDependencyGraph())
						.logTimings(false)
						.build())
				.localeParser(localeParser)
				.logger(logger)
				.build();
	}

	@Provides
	@Singleton
	TranslationRegistry provideTranslationRegistry() {
		return new InterceptTranslationRegistry();
	}

	@Provides
	@Singleton
	TranslationService<Locale> provideTranslationService(
			SemanticaConfiguration<Locale> configuration,
			TranslationRegistry registry
	) {
		return Semantica.createService(configuration, registry);
	}

	private Path ensureDirectory(Path path, String label) {
		try {
			Files.createDirectories(path);
			return path;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create " + label + " directory", e);
		}
	}
}
