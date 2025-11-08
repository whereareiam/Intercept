package me.whereareiam.intercept.interceptor;

/**
 * Base interface for interceptors.
 * Interceptors provide access to components through different methods:
 * packet libraries (PacketEvents, ProtocolLib), native platform APIs, or other mechanisms.
 *
 * <p>Interceptors are thin wrappers that extract data from components, delegate processing to
 * the common module, and write results back.</p>
 *
 * <p>Note: Metadata like name, availability, and priority are handled by the
 * {@link InterceptorProvider}, not the interceptor itself.</p>
 */
public interface Interceptor {
	/**
	 * Stops this interceptor and cleans up any resources.
	 */
	void shutdown();
}