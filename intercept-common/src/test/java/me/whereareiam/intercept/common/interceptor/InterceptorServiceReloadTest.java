package me.whereareiam.intercept.common.interceptor;

import com.google.inject.Provider;
import me.whereareiam.intercept.Registry;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.interceptor.Interceptor;
import me.whereareiam.intercept.interceptor.InterceptorProvider;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.type.ComponentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class InterceptorServiceReloadTest {

	private InterceptorRegistry registry;
	private Provider<Interception> interceptionProvider;
	private Registry<Reloadable> reloadableRegistry;
	private InterceptorService interceptorService;

	@BeforeAll
	static void initLogger() {
		// Initialize Logger with a mock to prevent NPEs
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);
	}

	@BeforeEach
	void setUp() {
		registry = mock(InterceptorRegistry.class);
		interceptionProvider = mock(Provider.class);
		reloadableRegistry = mock(Registry.class);

		interceptorService = new InterceptorService(
				registry,
				interceptionProvider,
				reloadableRegistry
		);
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(interceptorService);
	}

	@Test
	void shouldShutdownExistingInterceptorsOnReload() {
		// Setup interception config with enabled component
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		// Setup mock provider and interceptor
		InterceptorProvider provider = mock(InterceptorProvider.class);
		Interceptor interceptor = mock(Interceptor.class);
		when(registry.getBestProvider(any())).thenReturn(provider);
		when(provider.createInterceptor(any())).thenReturn(interceptor);

		// Reload
		interceptorService.reload();

		// Should shutdown all interceptors
		verify(registry).shutdownAllInterceptors();
	}

	@Test
	void shouldReinitializeInterceptorsAfterReload() {
		// Setup interception config
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		// Setup mock provider
		InterceptorProvider provider = mock(InterceptorProvider.class);
		Interceptor interceptor = mock(Interceptor.class);
		when(registry.getBestProvider(any())).thenReturn(provider);
		when(provider.createInterceptor(any())).thenReturn(interceptor);

		// Reload
		interceptorService.reload();

		// Should reinitialize - verify shutdown was called
		verify(registry).shutdownAllInterceptors();

		// Should create new interceptor and set it as active
		verify(provider).createInterceptor(any());
		verify(registry).setActiveInterceptor(any(), eq(interceptor));
	}

	@Test
	void shouldHandleReloadWithNoConfiguration() {
		// Return null configuration
		when(interceptionProvider.get()).thenReturn(null);

		// Should not throw exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should still shutdown
		verify(registry).shutdownAllInterceptors();
	}

	@Test
	void shouldHandleReloadWithDisabledComponents() {
		// Return config with disabled components
		Interception interception = createMockInterception(false);
		when(interceptionProvider.get()).thenReturn(interception);

		// Reload
		interceptorService.reload();

		// Should shutdown but not try to create new interceptors for disabled components
		verify(registry).shutdownAllInterceptors();
		verify(registry, never()).setActiveInterceptor(any(), any());
	}

	@Test
	void shouldHandleMultipleReloads() {
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		InterceptorProvider provider = mock(InterceptorProvider.class);
		Interceptor interceptor = mock(Interceptor.class);
		when(registry.getBestProvider(any())).thenReturn(provider);
		when(provider.createInterceptor(any())).thenReturn(interceptor);

		// Multiple reloads
		interceptorService.reload();
		interceptorService.reload();
		interceptorService.reload();

		// Should shutdown 3 times
		verify(registry, times(3)).shutdownAllInterceptors();
	}

	@Test
	void shouldHandleReloadWithNoAvailableProvider() {
		// Return config with enabled components
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		// No provider available
		when(registry.getBestProvider(any())).thenReturn(null);

		// Should not throw exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should still shutdown
		verify(registry).shutdownAllInterceptors();
	}

	@Test
	void shouldHandleReloadWhenProviderReturnsNull() {
		// Return config with enabled components
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		// Provider returns null interceptor
		InterceptorProvider provider = mock(InterceptorProvider.class);
		when(provider.getName()).thenReturn("TestProvider");
		when(registry.getBestProvider(any())).thenReturn(provider);
		when(provider.createInterceptor(any())).thenReturn(null);

		// Should not throw exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should shutdown but not set active interceptor
		verify(registry).shutdownAllInterceptors();
		verify(registry, never()).setActiveInterceptor(any(), any());
	}

	@Test
	void shouldHandleReloadWhenProviderThrowsException() {
		// Return config with enabled components
		Interception interception = createMockInterception(true);
		when(interceptionProvider.get()).thenReturn(interception);

		// Provider throws exception
		InterceptorProvider provider = mock(InterceptorProvider.class);
		when(provider.getName()).thenReturn("TestProvider");
		when(registry.getBestProvider(any())).thenReturn(provider);
		when(provider.createInterceptor(any())).thenThrow(new RuntimeException("Provider error"));

		// Should not propagate exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should still shutdown
		verify(registry).shutdownAllInterceptors();
	}

	private Interception createMockInterception(boolean enabled) {
		Interception interception = mock(Interception.class);
		Map<ComponentType, InterceptedComponent> components = new EnumMap<>(ComponentType.class);

		InterceptedComponent chatComponent = mock(InterceptedComponent.class);
		when(chatComponent.isEnabled()).thenReturn(enabled);
		components.put(ComponentType.CHAT, chatComponent);

		when(interception.getComponents()).thenReturn(components);
		return interception;
	}
}

