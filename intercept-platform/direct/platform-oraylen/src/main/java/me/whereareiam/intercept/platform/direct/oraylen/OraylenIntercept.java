package me.whereareiam.intercept.platform.direct.oraylen;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.dependency.DependencyResolver;
import me.whereareiam.intercept.common.CommonDependencyResolver;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.platform.direct.oraylen.inject.OraylenInjector;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.oraylen.api.annotation.OraylenExtension;
import net.oraylen.api.loader.extension.Extension;
import net.oraylen.api.model.library.LibraryDescriptor;
import net.oraylen.api.translation.TranslationEngine;
import net.oraylen.api.translation.TranslationEngineProvider;
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
	private final Injector platformInjector;
	private final Logger platformLogger = LoggerFactory.getLogger(OraylenIntercept.class);
	private final OraylenDependencyLoader dependencyLoader = new OraylenDependencyLoader(false);
	private final DependencyResolver dependencyResolver = new CommonDependencyResolver(dependencyLoader);
	private OraylenInjector interceptInjector;
	private boolean dependenciesResolved;

	@Inject
	public OraylenIntercept(
			@Named("extensionPath") Path extensionPath,
			SerializerEngine serializerEngine,
			Provider<net.oraylen.api.model.config.Settings> settingsProvider,
			Injector platformInjector
	) {
		this.extensionPath = extensionPath;
		this.serializerEngine = serializerEngine;
		this.settingsProvider = settingsProvider;
		this.platformInjector = platformInjector;
	}

	@Override
	public void onLoad() {
		interceptInjector = new OraylenInjector(
				extensionPath,
				serializerEngine,
				settingsProvider,
				platformInjector,
				platformLogger
		);

		LoggingHelper loggingHelper = interceptInjector.getInjector().getInstance(LoggingHelper.class);
		me.whereareiam.intercept.logging.Logger.init(loggingHelper);
	}

	@Override
	public TranslationEngine create() {
		return interceptInjector.getInjector().getInstance(TranslationEngine.class);
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
		ensureDependenciesResolved();
		return dependencyLoader.repositories();
	}

	@Override
	public List<LibraryDescriptor> libraries() {
		ensureDependenciesResolved();
		return dependencyLoader.libraries();
	}

	private void ensureDependenciesResolved() {
		if (dependenciesResolved)
			return;

		dependencyResolver.loadLibraries();
		dependencyResolver.resolveDependencies();
		dependenciesResolved = true;
	}
}
