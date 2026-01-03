package me.whereareiam.intercept.platform.interception.interceptor.kick;

import me.whereareiam.intercept.platform.interception.interceptor.Interceptor;

/**
 * Interceptor for kick/disconnect components.
 * Implementations handle platform-specific access while delegating
 * business logic to the {@link KickInterceptionProcessor}.
 *
 * <p>Interceptors are automatically initialized when created and cleaned up via {@link #shutdown()}.</p>
 */
public interface KickInterceptor extends Interceptor {
}