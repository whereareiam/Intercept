package me.whereareiam.intercept.platform.interception.interceptor.actionbar;

import me.whereareiam.intercept.platform.interception.interceptor.Interceptor;

/**
 * Interceptor for action bar components.
 * Implementations handle platform-specific access while delegating
 * business logic to the {@link ActionBarInterceptionProcessor}.
 *
 * <p>Interceptors are automatically initialized when created and cleaned up via {@link #shutdown()}.</p>
 */
public interface ActionBarInterceptor extends Interceptor {}