package me.whereareiam.intercept.platform.paper.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.adapter.database.DatabaseConfiguration;
import me.whereareiam.intercept.adapter.command.CommandConfiguration;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.intercept.platform.interception.InterceptionConfiguration;
import me.whereareiam.intercept.platform.interception.bukkit.common.PlatformConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

@Getter
public class PaperInjector {
	private final Injector injector;

	public PaperInjector(Plugin plugin, Path dataPath, LibraryManager libraryManager) {
		this.injector = Guice.createInjector(
				new PaperInjectorConfiguration(plugin, libraryManager),
				new PlatformConfiguration(),
				new CommonConfiguration(dataPath),
				new InterceptionConfiguration(),
				new CommandConfiguration(),
				new DatabaseConfiguration()
		);
	}
}
