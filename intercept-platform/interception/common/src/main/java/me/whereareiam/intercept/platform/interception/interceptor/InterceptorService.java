package me.whereareiam.intercept.platform.interception.interceptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.ComponentType;

import java.util.Map;

/**
 * Service for managing interceptors lifecycle.
 * Handles initialization, reloading, and shutdown of interceptors based on configuration.
 */
@Singleton
public class InterceptorService implements Reloadable {
	private final InterceptorRegistry registry;
	private final Provider<Interception> interceptionProvider;

	@Inject
	public InterceptorService(
			InterceptorRegistry registry,
			Provider<Interception> interceptionProvider,
			Registry<Reloadable> reloadableRegistry
	) {
		this.registry = registry;
		this.interceptionProvider = interceptionProvider;

		// Register self as reloadable
		reloadableRegistry.register(this);
	}

	/**
	 * Initializes interceptors based on the current configuration.
	 * Only starts interceptors for components that are enabled.
	 */
	public void initialize() {
		Interception interception = interceptionProvider.get();
		if (interception == null || interception.getComponents() == null) {
			Logger.warn("No interception configuration found, skipping interceptor initialization");
			return;
		}

		Map<ComponentType, InterceptedComponent> components = interception.getComponents();

		// Process each component type
		for (Map.Entry<ComponentType, InterceptedComponent> entry : components.entrySet()) {
			ComponentType type = entry.getKey();
			InterceptedComponent component = entry.getValue();

			if (!component.isEnabled()) {
				Logger.debug("Component %s is disabled, skipping", type);
				continue;
			}

			initializeInterceptor(type);
		}

		Logger.info("Interceptor initialization completed");
	}

	/**
	 * Initializes an interceptor for a specific component type.
	 *
	 * @param type The component type
	 */
	private void initializeInterceptor(ComponentType type) {
		InterceptorProvider provider = registry.getBestProvider(type);

		if (provider == null) {
			Logger.warn("No interceptor provider available for component: %s", type);
			return;
		}

		try {
			// Provider creates and initializes the interceptor
			Interceptor interceptor = provider.createInterceptor(type);
			if (interceptor == null) {
				Logger.severe("Provider %s returned null interceptor for component: %s",
						provider.getName(), type);
				return;
			}

			// Register the interceptor (already initialized by provider)
			registry.setActiveInterceptor(type, interceptor);
		} catch (Exception e) {
			Logger.severe("Failed to initialize interceptor for component " + type + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down all active interceptors.
	 */
	public void shutdown() {
		Logger.info("Shutting down interceptors...");
		registry.shutdownAllInterceptors();
		Logger.info("Interceptors shutdown completed");
	}

	@Override
	public void reload() {
		Logger.info("Reloading interceptors...");

		// Shutdown existing interceptors
		registry.shutdownAllInterceptors();

		// Reinitialize interceptors with new configuration
		initialize();

		Logger.info("Interceptors reloaded successfully");
	}
}