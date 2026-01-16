package me.whereareiam.intercept.platform.direct.oraylen.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import lombok.Getter;
import me.whereareiam.intercept.adapter.command.CommandConfiguration;
import me.whereareiam.intercept.adapter.database.DatabaseConfiguration;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.keystone.Actor;
import net.oraylen.api.translation.file.TranslationFileCodecRegistry;
import net.oraylen.api.model.config.Settings;
import org.incendo.cloud.CommandManager;
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
			Logger logger,
			CommandManager<Actor> platformCommandManager,
			TranslationFileCodecRegistry oraylenCodecRegistry
	) {
		this.injector = Guice.createInjector(
				new CommonConfiguration(extensionPath),
				new OraylenInjectorConfiguration(
						Objects.requireNonNull(extensionPath, "extensionPath"),
						Objects.requireNonNull(serializerEngine, "serializerEngine"),
						Objects.requireNonNull(settingsProvider, "settingsProvider"),
						Objects.requireNonNull(logger, "logger"),
						Objects.requireNonNull(platformCommandManager, "platformCommandManager"),
						Objects.requireNonNull(oraylenCodecRegistry, "oraylenCodecRegistry")
				),
				new DatabaseConfiguration(),
				new CommandConfiguration()
		);
	}
}
