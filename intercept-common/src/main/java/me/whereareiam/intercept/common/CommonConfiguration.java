package me.whereareiam.intercept.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import me.whereareiam.intercept.Registry;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.ConfiguraBootstrap;
import me.whereareiam.intercept.common.config.resolver.FileSystemConfigurationTypeResolver;
import me.whereareiam.intercept.common.event.EventController;
import me.whereareiam.intercept.common.provider.ReloadableProvider;
import me.whereareiam.intercept.common.provider.config.InterceptionProvider;
import me.whereareiam.intercept.common.provider.config.SettingsProvider;
import me.whereareiam.intercept.common.updater.provider.GitHubProvider;
import me.whereareiam.intercept.common.updater.provider.ModrinthProvider;
import me.whereareiam.intercept.common.updater.provider.SpigotMCProvider;
import me.whereareiam.intercept.config.ConfigurationTypeResolver;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.type.ProviderType;
import me.whereareiam.intercept.updater.UpdateProvider;
import me.whereareiam.intercept.util.EventUtil;

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
		// Configuration
		bind(ConfigurationTypeResolver.class)
				.to(FileSystemConfigurationTypeResolver.class)
				.asEagerSingleton();
		bind(ConfiguraBootstrap.class).asEagerSingleton();

		// Configs
		bind(SettingsProvider.class);
		bind(Settings.class).toProvider(SettingsProvider.class);
		bind(InterceptionProvider.class);
		bind(Interception.class).toProvider(InterceptionProvider.class);

		// Services
		bind(EventManager.class).to(EventController.class);
		bind(EventUtil.class).asEagerSingleton();

		// Plugin
		bind(Intercept.class).asEagerSingleton();

		// Updater
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.MODRINTH.toString()))
				.to(ModrinthProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.GITHUB.toString()))
				.to(GitHubProvider.class);
		bind(UpdateProvider.class).annotatedWith(Names.named(ProviderType.SPIGOT.toString()))
				.to(SpigotMCProvider.class);

		// Other
		bind(new TypeLiteral<Registry<Reloadable>>() {
		}).to(ReloadableProvider.class).asEagerSingleton();
		bind(new TypeLiteral<Set<Reloadable>>() {
		}).annotatedWith(Names.named("reloadables")).toProvider(ReloadableProvider.class).asEagerSingleton();
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

	private Path ensureDirectory(Path path, String label) {
		try {
			Files.createDirectories(path);
			return path;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create " + label + " directory", e);
		}
	}
}
