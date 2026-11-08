package me.whereareiam.intercept.platform.paper;

import me.whereareiam.attache.platform.paper.PaperLibraryManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Intercept on Paper. Libraries load before anything else, so this class only names Attache: Paper verifies it
 * before {@link #onLoad()} runs, when Guice and the rest are not loaded yet.
 */
@SuppressWarnings("unused")
public final class PaperIntercept extends JavaPlugin {
	private PaperLibraryManager libraries;
	private PaperRuntime runtime;

	@Override
	public void onLoad() {
		libraries = new PaperLibraryManager(this, ".libraries");
		libraries.loadDescriptors();
	}

	@Override
	public void onEnable() {
		runtime = new PaperRuntime(this, libraries);
		if (!runtime.start()) getServer().getPluginManager().disablePlugin(this);
	}

	@Override
	public void onDisable() {
		if (runtime != null) runtime.stop();
		runtime = null;
	}
}
