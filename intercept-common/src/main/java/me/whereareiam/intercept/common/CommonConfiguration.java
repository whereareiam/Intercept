package me.whereareiam.intercept.common;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.common.config.ConfiguraBootstrap;
import me.whereareiam.intercept.common.config.resolver.FileSystemConfigurationTypeResolver;
import me.whereareiam.intercept.common.event.EventController;
import me.whereareiam.intercept.common.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.common.interceptor.InterceptorService;
import me.whereareiam.intercept.common.interceptor.processor.DefaultActionBarInterceptionProcessor;
import me.whereareiam.intercept.common.interceptor.processor.DefaultChatInterceptionProcessor;
import me.whereareiam.intercept.common.interceptor.processor.DefaultKickInterceptionProcessor;
import me.whereareiam.intercept.common.listener.InspectionModeEnhancer;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.InterceptSemanticaLogger;
import me.whereareiam.intercept.common.messaging.MessageLifecycleService;
import me.whereareiam.intercept.common.messaging.interception.DefaultInterceptionRegistry;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageDataService;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileWriter;
import me.whereareiam.intercept.common.messaging.regex.DefaultRegexMatchingService;
import me.whereareiam.intercept.common.messaging.tag.DefaultTagReplacementService;
import me.whereareiam.intercept.common.player.DefaultPlayerRegistry;
import me.whereareiam.intercept.common.provider.IntegrationProvider;
import me.whereareiam.intercept.common.provider.ReloadableProvider;
import me.whereareiam.intercept.common.provider.SerializerEngineProvider;
import me.whereareiam.intercept.common.provider.config.*;
import me.whereareiam.intercept.common.updater.provider.GitHubProvider;
import me.whereareiam.intercept.common.updater.provider.ModrinthProvider;
import me.whereareiam.intercept.common.updater.provider.SpigotMCProvider;
import me.whereareiam.intercept.common.util.MessageTags;
import me.whereareiam.intercept.config.ConfigurationTypeResolver;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.integration.Integration;
import me.whereareiam.intercept.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.messaging.*;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.config.*;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.ProviderType;
import me.whereareiam.intercept.updater.UpdateProvider;
import me.whereareiam.intercept.util.EventUtil;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.semantica.SemanticaLogger;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.SemanticLocale;
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
		bind(InterceptionProvider.class);
		bind(Interception.class).toProvider(InterceptionProvider.class);

		// Services
		bind(SerializerEngine.class).toProvider(SerializerEngineProvider.class);
		bind(EventManager.class).to(EventController.class);
		bind(EventUtil.class).asEagerSingleton();
		bind(PlayerRegistry.class).to(DefaultPlayerRegistry.class);

		// Plugin
		bind(Intercept.class).asEagerSingleton();

		// Messages system
		bind(MessageRegistry.class).to(DefaultMessageRegistry.class);
		bind(InterceptionRegistry.class).to(DefaultInterceptionRegistry.class).asEagerSingleton();
		bind(MessageFileLoader.class).to(DefaultMessageFileLoader.class);
		bind(MessageFileWriter.class).to(DefaultMessageFileWriter.class);
		bind(MessageDataService.class).to(DefaultMessageDataService.class);
		bind(MessageLifecycleService.class).asEagerSingleton();
		bind(TagReplacementService.class).to(DefaultTagReplacementService.class);
		bind(RegexMatchingService.class).to(DefaultRegexMatchingService.class);

		// Interceptors
		bind(InterceptorRegistry.class).asEagerSingleton();
		bind(ChatInterceptionProcessor.class).to(DefaultChatInterceptionProcessor.class).asEagerSingleton();
		bind(ActionBarInterceptionProcessor.class).to(DefaultActionBarInterceptionProcessor.class).asEagerSingleton();
		bind(KickInterceptionProcessor.class).to(DefaultKickInterceptionProcessor.class).asEagerSingleton();
		bind(InterceptorService.class).asEagerSingleton();
		// Updater
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.MODRINTH.toString()))
				.to(ModrinthProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.GITHUB.toString()))
				.to(GitHubProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.SPIGOT.toString()))
				.to(SpigotMCProvider.class);

		// Listeners
		bind(InspectionModeEnhancer.class).asEagerSingleton();

		// Other
		bind(new TypeLiteral<me.whereareiam.intercept.registry.base.Registry<Integration>>() {
		}).to(IntegrationProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Integration>>() {
		}).toProvider(IntegrationProvider.class).asEagerSingleton();

		bind(new TypeLiteral<Registry<Reloadable>>() {
		}).to(ReloadableProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Reloadable>>() {
		}).annotatedWith(Names.named("reloadables")).toProvider(ReloadableProvider.class).asEagerSingleton();
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

		TagConfiguration tags = TagConfiguration.builder()
				.referencePrefix(MessageTags.MESSAGE_REF_PREFIX)
				.placeholderPrefix(MessageTags.PLACEHOLDER_PREFIX)
				.conditionalIf(MessageTags.CONDITIONAL_IF)
				.conditionalElse(MessageTags.CONDITIONAL_ELSE)
				.build();

		return SemanticaConfiguration.<Locale>builder()
				.defaultLocale(SemanticLocale.wrap(settings.getLocale()))
				.tagConfiguration(tags)
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
	TranslationService<Locale> provideTranslationService(
			SemanticaConfiguration<Locale> configuration
	) {
		return Semantica.createService(configuration);
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
