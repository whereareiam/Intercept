package me.whereareiam.intercept.platform.paper.inject;

import com.google.inject.Injector;
import lombok.Getter;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.adapter.database.DatabaseConfiguration;
import me.whereareiam.intercept.command.CommandConfiguration;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.intercept.common.InjectorFactory;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.platform.common.PlatformConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.List;

@Getter
public class PaperInjector {
	private final Injector injector;

	public PaperInjector(Plugin plugin, Path dataPath, LibraryManager libraryManager) {
		this.injector = InjectorFactory.createInjector(
				dataPath,
				List.of(new InjectorFactory.ConditionalModule(
						injector -> injector.getInstance(Persistence.class).isEnabled(),
						DatabaseConfiguration::new
				)),
				new PaperInjectorConfiguration(plugin, libraryManager),
				new PlatformConfiguration(),
				new CommonConfiguration(dataPath),
				new CommandConfiguration()
		);
	}
}
