package me.whereareiam.intercept.platform.paper.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.intercept.common.CommonInjector;
import me.whereareiam.intercept.platform.common.PlatformConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

@Getter
public class PaperInjector {
	public PaperInjector(Plugin plugin, Path dataPath) {
		Injector injector = Guice.createInjector(
				new PaperInjectorConfiguration(plugin),
				new PlatformConfiguration(),
				new CommonConfiguration(dataPath)
		);

		CommonInjector.setInjector(injector);
	}
}
