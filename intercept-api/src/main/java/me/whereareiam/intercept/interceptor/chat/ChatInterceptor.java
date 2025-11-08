package me.whereareiam.intercept.interceptor.chat;

import me.whereareiam.intercept.interceptor.Interceptor;

/**
 * Interceptor for chat components.
 * Implementations handle platform-specific access while delegating
 * business logic to the {@link ChatInterceptionProcessor}.
 *
 * <p>Interceptors are automatically initialized when created and cleaned up via {@link #shutdown()}.</p>
 */
public interface ChatInterceptor extends Interceptor {
}