package me.whereareiam.intercept.platform.interception.interceptor;

import com.google.inject.Singleton;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.type.ComponentType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing interceptor providers and active interceptors.
 * Handles provider registration, interceptor selection, and lifecycle management.
 */
@Singleton
public class InterceptorRegistry {
	private final Map<ComponentType, List<InterceptorProvider>> providers = new ConcurrentHashMap<>();
	private final Map<ComponentType, Interceptor> activeInterceptors = new ConcurrentHashMap<>();

	/**
	 * Registers a provider for specific component types.
	 *
	 * @param provider The provider to register
	 */
	public void registerProvider(InterceptorProvider provider) {
		if (!provider.isAvailable()) {
			Logger.debug("Skipping unavailable provider: %s", provider.getName());
			return;
		}

		for (ComponentType type : provider.getSupportedComponents()) {
			providers.computeIfAbsent(type, k -> new ArrayList<>()).add(provider);
		}

		Logger.debug("Registered interceptor provider: %s (priority: %s, components: %s)",
				provider.getName(), provider.getPriority(), provider.getSupportedComponents());
	}

	/**
	 * Gets the best available provider for a component type.
	 * Providers are selected based on availability and priority.
	 *
	 * @param type The component type
	 * @return The best provider, or null if none available
	 */
	public InterceptorProvider getBestProvider(ComponentType type) {
		List<InterceptorProvider> typeProviders = providers.get(type);
		if (typeProviders == null || typeProviders.isEmpty())
			return null;

		return typeProviders.stream()
				.filter(InterceptorProvider::isAvailable)
				.max(Comparator.comparingInt(InterceptorProvider::getPriority))
				.orElse(null);
	}

	/**
	 * Sets the active interceptor for a component type.
	 *
	 * @param type        The component type
	 * @param interceptor The interceptor to set as active
	 */
	public void setActiveInterceptor(ComponentType type, Interceptor interceptor) {
		activeInterceptors.put(type, interceptor);
	}

	/**
	 * Gets all active interceptors.
	 *
	 * @return Map of component types to active interceptors
	 */
	public Map<ComponentType, Interceptor> getActiveInterceptors() {
		return new HashMap<>(activeInterceptors);
	}

	/**
	 * Shuts down all active interceptors.
	 */
	public void shutdownAllInterceptors() {
		activeInterceptors.forEach((type, interceptor) -> {
			try {
				interceptor.shutdown();
				Logger.debug("Shutdown %s interceptor", type);
			} catch (Exception e) {
				Logger.severe("Failed to shutdown %s interceptor", type);
			}
		});
		activeInterceptors.clear();
	}
}