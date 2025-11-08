package me.whereareiam.intercept.interceptor;

import me.whereareiam.intercept.type.InterceptedComponentType;

import java.util.Set;

/**
 * Provider interface for creating interceptors.
 * Each implementation represents a specific library or API (PacketEvents, ProtocolLib, native, etc.).
 */
public interface InterceptorProvider {
	/**
	 * Gets the name of this provider.
	 *
	 * @return The provider name
	 */
	String getName();

	/**
	 * Gets the priority of interceptors created by this provider.
	 * Higher priority providers are preferred when multiple are available.
	 *
	 * @return The priority value
	 */
	int getPriority();

	/**
	 * Checks if this provider's underlying library or API is available.
	 *
	 * @return true if the provider can create interceptors, false otherwise
	 */
	boolean isAvailable();

	/**
	 * Gets the set of component types supported by this provider.
	 *
	 * @return Set of supported component types
	 */
	Set<InterceptedComponentType> getSupportedComponents();

	/**
	 * Creates an interceptor for the specified component type.
	 *
	 * @param type The component type
	 * @return The interceptor, or null if not supported
	 */
	Interceptor createInterceptor(InterceptedComponentType type);
}