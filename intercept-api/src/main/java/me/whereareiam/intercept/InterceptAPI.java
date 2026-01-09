package me.whereareiam.intercept;

import com.google.inject.Injector;
import com.google.inject.Key;
import lombok.Getter;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.lifecycle.InterceptStartedEvent;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.semantica.translation.TranslationService;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
/**
 * Main API access point for the Intercept plugin.
 *
 * <p>External plugins should use this class to access Intercept services.
 * All services become available after the {@link InterceptStartedEvent}
 * is fired.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Check if API is ready
 * if (!InterceptAPI.isInitialized()) {
 *     getLogger().warning("Intercept not ready yet!");
 *     return;
 * }
 *
 * // Get the translation service
 * TranslationService<Locale> translationService = InterceptAPI.getTranslationService();
 * String message = translationService.resolve("my.key", Locale.ENGLISH);
 *
 * // Or get any service by class
 * PlayerRegistry registry = InterceptAPI.getService(PlayerRegistry.class);
 * }</pre>
 *
 * <p><b>Important:</b> Always check {@link #isInitialized()} before accessing services,
 * or wait for {@link InterceptStartedEvent}.</p>
 */
public final class InterceptAPI {
	private static volatile Injector injector;
	@Getter
	private static volatile boolean initialized = false;

	private InterceptAPI() {
		throw new UnsupportedOperationException("This class cannot be instantiated");
	}

	/**
	 * Initializes the API with the Guice injector.
	 * <p>This method is called internally by Intercept during startup.
	 * External plugins should never call this method.</p>
	 *
	 * @param injector the Guice injector
	 * @throws IllegalStateException if already initialized
	 */
	public static void initialize(@NotNull Injector injector) {
		if (InterceptAPI.injector != null) {
			throw new IllegalStateException("InterceptAPI is already initialized");
		}
		InterceptAPI.injector = injector;
		InterceptAPI.initialized = true;
	}

	/**
	 * Shuts down the API and clears the injector reference.
	 * <p>This method is called internally by Intercept during shutdown.
	 * External plugins should never call this method.</p>
	 */
	public static void shutdown() {
		InterceptAPI.injector = null;
		InterceptAPI.initialized = false;
	}

	/**
	 * Gets a service instance from the Intercept API.
	 *
	 * @param serviceClass the service class to retrieve
	 * @param <T>          the service type
	 * @return the service instance
	 * @throws IllegalStateException if the API is not initialized
	 */
	@NotNull
	public static <T> T getService(@NotNull Class<T> serviceClass) {
		Injector currentInjector = injector;
		if (currentInjector == null) {
			throw new IllegalStateException(
					"InterceptAPI is not initialized. Make sure Intercept is loaded and wait for InterceptStartedEvent."
			);
		}
		return currentInjector.getInstance(serviceClass);
	}

	// ===== Convenience Methods for Common Services =====

	/**
	 * Gets the EventManager for registering listeners and calling events.
	 *
	 * @return the EventManager instance
	 * @throws IllegalStateException if the API is not initialized
	 */
	@NotNull
	public static EventManager getEventManager() {
		return getService(EventManager.class);
	}

	/**
	 * Gets the TranslationService for resolving localized messages.
	 *
	 * @return the TranslationService instance
	 * @throws IllegalStateException if the API is not initialized
	 */
	@NotNull
	public static TranslationService<Locale> getTranslationService() {
		Injector currentInjector = injector;
		if (currentInjector == null) {
			throw new IllegalStateException(
					"InterceptAPI is not initialized. Make sure Intercept is loaded and wait for InterceptStartedEvent."
			);
		}
		return currentInjector.getInstance(new Key<TranslationService<Locale>>() {});
	}

	/**
	 * Gets the PlayerRegistry for accessing player data.
	 *
	 * @return the PlayerRegistry instance
	 * @throws IllegalStateException if the API is not initialized
	 */
	@NotNull
	public static PlayerRegistry getPlayerRegistry() {
		return getService(PlayerRegistry.class);
	}

	/**
	 * Gets the CommandService for command operations.
	 *
	 * @return the CommandService instance
	 * @throws IllegalStateException if the API is not initialized
	 */
	@NotNull
	public static CommandService getCommandService() {
		return getService(CommandService.class);
	}
}
