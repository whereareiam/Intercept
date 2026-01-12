package me.whereareiam.intercept.platform.direct.oraylen;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.common.tag.serializer.TagProcessingDecorator;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.lifecycle.InterceptBootstrappedEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.type.PluginType;
import me.whereareiam.intercept.platform.direct.oraylen.inject.OraylenInjector;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.keystone.Serializers;
import net.oraylen.api.annotation.OraylenExtension;
import net.oraylen.api.loader.extension.Extension;
import net.oraylen.api.model.library.LibraryDescriptor;
import net.oraylen.api.translation.TranslationEngine;
import net.oraylen.api.translation.TranslationEngineProvider;
import org.incendo.cloud.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.net.URI;
import java.util.List;

@OraylenExtension
public final class OraylenIntercept extends Extension implements TranslationEngineProvider {
	private final Path extensionPath;
	private final SerializerEngine serializerEngine;
	private final Provider<net.oraylen.api.model.config.Settings> settingsProvider;
	private final CommandManager<Actor> platformCommandManager;
	private final Logger platformLogger = LoggerFactory.getLogger(OraylenIntercept.class);
	private final OraylenDependencyLoader dependencyLoader;

	private OraylenInjector injector;

	@Inject
	public OraylenIntercept(
			@Named("extensionPath") Path extensionPath,
			SerializerEngine serializerEngine,
			Provider<net.oraylen.api.model.config.Settings> settingsProvider,
			CommandManager<Actor> platformCommandManager
	) {
		this.extensionPath = extensionPath;
		this.serializerEngine = serializerEngine;
		this.settingsProvider = settingsProvider;
		this.platformCommandManager = platformCommandManager;

		this.dependencyLoader = new OraylenDependencyLoader(false);
	}

	@Override
	public void onLoad() {
		PluginType.setPluginType(PluginType.ORAYLEN);

		injector = new OraylenInjector(
				extensionPath,
				serializerEngine,
				settingsProvider,
				platformLogger,
				platformCommandManager
		);

		LoggingHelper loggingHelper = injector.getInjector().getInstance(LoggingHelper.class);
		me.whereareiam.intercept.logging.Logger.init(loggingHelper);
		EventManager eventManager = injector.getInjector().getInstance(EventManager.class);
		eventManager.call(new InterceptBootstrappedEvent());

		// Register tag processing decorator with Oraylen's serializer engine using public API
		TagProcessingDecorator decorator = injector.getInjector().getInstance(TagProcessingDecorator.class);
		Serializers.registerDecorator(serializerEngine, decorator);
	}

	@Override
	public void onEnable() {
		if (injector == null) return;
		EventManager eventManager = injector.getInjector().getInstance(EventManager.class);
		eventManager.call(new InterceptReadyEvent());
	}

	@Override
	public void onDisable() {
		if (injector == null) return;
		EventManager eventManager = injector.getInjector().getInstance(EventManager.class);
		eventManager.call(new InterceptShutdownEvent());
	}

	@Override
	public TranslationEngine create() {
		return injector.getInjector().getInstance(TranslationEngine.class);
	}

	@Override
	public int priority() {
		return 100;
	}

	@Override
	public String name() {
		return "Intercept";
	}

	@Override
	public List<URI> repositories() {
		return dependencyLoader.repositories();
	}

	@Override
	public List<LibraryDescriptor> libraries() {
		return dependencyLoader.libraries();
	}
}
