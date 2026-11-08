package me.whereareiam.intercept.platform.paper;

import com.google.inject.Injector;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.common.Intercept;
import me.whereareiam.intercept.integration.placeholderapi.PlaceholderApiConfiguration;
import me.whereareiam.intercept.platform.InterceptInjector;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Intercept while the plugin is enabled: built once its libraries are loaded.
 */
final class PaperRuntime {
	private final JavaPlugin plugin;
	private final Intercept intercept;

	PaperRuntime(@NotNull JavaPlugin plugin, @NotNull LibraryManager libraries) {
		this.plugin = plugin;

		PaperConfiguration platform = new PaperConfiguration(plugin, libraries);
		Injector injector = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
				? InterceptInjector.create(platform, new PlaceholderApiConfiguration())
				: InterceptInjector.create(platform);
		this.intercept = injector.getInstance(Intercept.class);
	}

	/**
	 * Starts Intercept.
	 *
	 * @return whether it started; it logged why when it did not
	 */
	boolean start() {
		try {
			intercept.start();
			return true;
		} catch (RuntimeException failure) {
			plugin.getLogger().log(Level.SEVERE, "Intercept cannot start", failure);
			return false;
		}
	}

	void stop() {
		intercept.stop();
	}
}
