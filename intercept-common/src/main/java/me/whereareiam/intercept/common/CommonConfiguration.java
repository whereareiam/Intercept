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
import me.whereareiam.intercept.common.messaging.DefaultMessageService;
import me.whereareiam.intercept.common.messaging.DefaultTagReplacementService;
import me.whereareiam.intercept.common.messaging.MessagesService;
import me.whereareiam.intercept.common.messaging.regex.DefaultRegexMatchingService;
import me.whereareiam.intercept.common.player.DefaultPlayerRegistry;
import me.whereareiam.intercept.common.provider.ReloadableProvider;
import me.whereareiam.intercept.common.provider.SerializerEngineProvider;
import me.whereareiam.intercept.common.provider.config.*;
import me.whereareiam.intercept.common.updater.provider.GitHubProvider;
import me.whereareiam.intercept.common.updater.provider.ModrinthProvider;
import me.whereareiam.intercept.common.updater.provider.SpigotMCProvider;
import me.whereareiam.intercept.config.ConfigurationTypeResolver;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.model.config.*;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.intercept.type.ProviderType;
import me.whereareiam.intercept.updater.UpdateProvider;
import me.whereareiam.intercept.util.EventUtil;
import me.whereareiam.keystone.serializer.SerializerEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
		bind(MessageService.class).to(DefaultMessageService.class);
		bind(MessagesService.class).asEagerSingleton();
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
		bind(new TypeLiteral<Registry<Reloadable>>() {
		}).to(ReloadableProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Reloadable>>() {
		}).annotatedWith(Names.named("reloadables")).toProvider(ReloadableProvider.class).asEagerSingleton();
	}

	@Inject
	void initializeSerializationHelper(Provider<SerializerEngine> serializerProvider) {
		Serializer.initialize(serializerProvider);
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

	private Path ensureDirectory(Path path, String label) {
		try {
			Files.createDirectories(path);
			return path;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create " + label + " directory", e);
		}
	}
}
