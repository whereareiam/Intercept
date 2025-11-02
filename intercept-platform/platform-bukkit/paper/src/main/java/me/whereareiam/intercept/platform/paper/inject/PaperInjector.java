package me.whereareiam.intercept.platform.paper.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.intercept.platform.common.PlatformConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

@Getter
public class PaperInjector {
	private final Injector injector;

	public PaperInjector(Plugin plugin, Path dataPath) {
		this.injector = Guice.createInjector(
				new PaperInjectorConfiguration(plugin),
				new PlatformConfiguration(),
				new CommonConfiguration(dataPath)
		);
	}
}
