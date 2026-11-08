package me.whereareiam.intercept.platform;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.library.LibraryLoader;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * What a platform has to provide: its folder, its logger, how it loads libraries, how it applies a chosen language
 * and the Cloud command manager Intercept's commands go on.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PlatformConfiguration extends AbstractModule {
	private final @NotNull Path dataPath;
	private final @NotNull Logger logger;

	@Override
	protected final void configure() {
		bind(Path.class).annotatedWith(Names.named("dataPath")).toInstance(dataPath);
		bind(Logger.class).annotatedWith(Names.named("intercept")).toInstance(logger);
		bind(LibraryLoader.class).to(libraryLoader());
		bind(LanguageApplier.class).to(languageApplier());
		bind(new TypeLiteral<CommandManager<CommandSource>>() {
		}).toProvider(commandManager());

		configurePlatform();
	}

	/**
	 * Binds what only this platform has, such as its listeners and packet library.
	 */
	protected abstract void configurePlatform();

	/**
	 * Returns how this platform loads runtime libraries.
	 *
	 * @return the loader's class
	 */
	protected abstract @NotNull Class<? extends LibraryLoader> libraryLoader();

	/**
	 * Returns how this platform makes a player see their chosen language.
	 *
	 * @return the applier's class
	 */
	protected abstract @NotNull Class<? extends LanguageApplier> languageApplier();

	/**
	 * Returns the provider of this platform's command manager.
	 *
	 * @return the provider's class
	 */
	protected abstract @NotNull Class<? extends com.google.inject.Provider<CommandManager<CommandSource>>> commandManager();
}
