package me.whereareiam.intercept.platform.direct.oraylen.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import lombok.Getter;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.oraylen.api.model.config.Settings;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Objects;

@Getter
public final class OraylenInjector {
	private final Injector injector;

	public OraylenInjector(
			Path extensionPath,
			SerializerEngine serializerEngine,
			Provider<Settings> settingsProvider,
			Injector platformInjector,
			Logger logger
	) {
		this.injector = Guice.createInjector(
				new OraylenInjectorConfiguration(
						Objects.requireNonNull(extensionPath, "extensionPath"),
						Objects.requireNonNull(serializerEngine, "serializerEngine"),
						Objects.requireNonNull(settingsProvider, "settingsProvider"),
						Objects.requireNonNull(platformInjector, "platformInjector"),
						Objects.requireNonNull(logger, "logger")
				)
		);
	}
}
