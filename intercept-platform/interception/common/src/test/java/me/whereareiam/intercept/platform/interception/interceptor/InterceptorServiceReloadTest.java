package me.whereareiam.intercept.platform.interception.interceptor;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.platform.interception.interceptor.base.Interceptor;
import me.whereareiam.intercept.platform.interception.interceptor.base.InterceptorProvider;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.intercept.type.ComponentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterceptorServiceReloadTest {
	@Mock
	private Provider<Interception> interceptionProvider;
	@Mock
	private Registry<Reloadable> reloadableRegistry;
	@Mock
	private InterceptorRegistry registry;
	@Mock
	private InterceptorProvider mockProvider;
	@Mock
	private Interceptor mockInterceptor;
	@Mock
	private Interception mockInterception;
	@Mock
	private InterceptedComponent mockComponent;

	private InterceptorService interceptorService;

	@BeforeAll
	static void initLogger() {
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);
	}

	@BeforeEach
	void setUp() {
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
		setupMockInterception(true);
		when(interceptionProvider.get()).thenReturn(mockInterception);

		// Setup mock provider and interceptor
		when(registry.getBestProvider(any())).thenReturn(mockProvider);
		when(mockProvider.createInterceptor(any())).thenReturn(mockInterceptor);

		// Reload
		interceptorService.reload();

		// Should shutdown all interceptors
		verify(registry).shutdownAllInterceptors();
	}

	@Test
	void shouldReinitializeInterceptorsAfterReload() {
		// Setup interception config
		setupMockInterception(true);
		when(interceptionProvider.get()).thenReturn(mockInterception);

		// Setup mock provider
		when(registry.getBestProvider(any())).thenReturn(mockProvider);
		when(mockProvider.createInterceptor(any())).thenReturn(mockInterceptor);

		// Reload
		interceptorService.reload();

		// Should reinitialize - verify shutdown was called
		verify(registry).shutdownAllInterceptors();

		// Should create new interceptor and set it as active
		verify(mockProvider).createInterceptor(any());
		verify(registry).setActiveInterceptor(any(), eq(mockInterceptor));
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
		setupMockInterception(false);
		when(interceptionProvider.get()).thenReturn(mockInterception);

		// Reload
		interceptorService.reload();

		// Should shutdown but not try to create new interceptors for disabled components
		verify(registry).shutdownAllInterceptors();
		verify(registry, never()).setActiveInterceptor(any(), any());
	}

	@Test
	void shouldHandleReloadWithNoAvailableProvider() {
		// Return config with enabled components
		setupMockInterception(true);
		when(interceptionProvider.get()).thenReturn(mockInterception);

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
		setupMockInterception(true);
		when(interceptionProvider.get()).thenReturn(mockInterception);

		// Provider returns null interceptor
		when(mockProvider.getName()).thenReturn("TestProvider");
		when(registry.getBestProvider(any())).thenReturn(mockProvider);
		when(mockProvider.createInterceptor(any())).thenReturn(null);

		// Should not throw exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should shutdown but not set active interceptor
		verify(registry).shutdownAllInterceptors();
		verify(registry, never()).setActiveInterceptor(any(), any());
	}

	@Test
	void shouldHandleReloadWhenProviderThrowsException() {
		// Return config with enabled components
		setupMockInterception(true);
		when(interceptionProvider.get()).thenReturn(mockInterception);

		// Provider throws exception
		when(registry.getBestProvider(any())).thenReturn(mockProvider);
		when(mockProvider.createInterceptor(any())).thenThrow(new RuntimeException("Provider error"));

		// Should not propagate exception
		assertDoesNotThrow(() -> interceptorService.reload());

		// Should still shutdown
		verify(registry).shutdownAllInterceptors();
	}

	private void setupMockInterception(boolean enabled) {
		Map<ComponentType, InterceptedComponent> components = new EnumMap<>(ComponentType.class);
		when(mockComponent.isEnabled()).thenReturn(enabled);
		components.put(ComponentType.CHAT, mockComponent);
		when(mockInterception.getComponents()).thenReturn(components);
	}
}
