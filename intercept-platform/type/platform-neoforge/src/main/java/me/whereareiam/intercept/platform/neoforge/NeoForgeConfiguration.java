package me.whereareiam.intercept.platform.neoforge;

import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.interception.library.mixin.PacketTranslation;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.library.LibraryLoader;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.platform.PlatformConfiguration;
import me.whereareiam.intercept.platform.neoforge.command.NeoForgeCommandManagerProvider;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * NeoForge's part: the game sends keys unrendered, and the Mixin library renders each outgoing system message in the
 * receiver's chosen language, so a choice needs nothing else to take effect.
 */
public final class NeoForgeConfiguration extends PlatformConfiguration {
	public NeoForgeConfiguration(@NotNull Path dataPath, @NotNull Logger logger) {
		super(dataPath, logger);
	}

	@Override
	protected void configurePlatform() {
		Multibinder<Lifecycle> lifecycles = Multibinder.newSetBinder(binder(), Lifecycle.class);
		lifecycles.addBinding().to(PacketTranslation.class);
		lifecycles.addBinding().to(NeoForgePlayers.class);
	}

	@Override
	protected @NotNull Class<? extends LibraryLoader> libraryLoader() {
		return BundledLibraries.class;
	}

	@Override
	protected @NotNull Class<? extends LanguageApplier> languageApplier() {
		return SendTimeLanguage.class;
	}

	@Override
	protected @NotNull Class<? extends com.google.inject.Provider<CommandManager<CommandSource>>> commandManager() {
		return NeoForgeCommandManagerProvider.class;
	}
}
