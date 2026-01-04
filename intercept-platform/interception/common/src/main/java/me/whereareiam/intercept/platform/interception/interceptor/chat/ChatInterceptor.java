package me.whereareiam.intercept.platform.interception.interceptor.chat;

import me.whereareiam.intercept.platform.interception.interceptor.base.Interceptor;

/**
 * Interceptor for chat components.
 * Implementations handle platform-specific access while delegating
 * business logic to the {@link ChatInterceptionProcessor}.
 *
 * <p>Interceptors are automatically initialized when created and cleaned up via {@link #shutdown()}.</p>
 */
public interface ChatInterceptor extends Interceptor {
}