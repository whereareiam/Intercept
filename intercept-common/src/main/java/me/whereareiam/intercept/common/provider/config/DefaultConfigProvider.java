package me.whereareiam.intercept.common.provider.config;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.config.ConfigProvider;
import me.whereareiam.intercept.registry.Registry;

import java.nio.file.Path;

/**
 * Adapter-layer base that wires {@link ConfigProvider} into our reload registry
 * and exposes the resolved base path for subclasses.
 */
public abstract class DefaultConfigProvider<T> extends ConfigProvider<T> implements Provider<T> {
	private final Path basePath;

	protected DefaultConfigProvider(Path basePath, Registry<Reloadable> reloadables) {
		this.basePath = basePath;
		reloadables.register(this);
	}

	protected Path getBasePath() {
		return basePath;
	}
}