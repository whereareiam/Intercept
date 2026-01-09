package me.whereareiam.intercept.platform.interception.regex;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegexMatchingServiceReloadTest {
	@Mock
	private Provider<Interception> settingsProvider;
	@Mock
	private TranslationService<Locale> translationService;
	@Mock
	private Registry<Reloadable> reloadableRegistry;
	@Mock
	private InterceptionRegistry registry;

	private RegexMatchingService regexMatchingService;

	@BeforeAll
	static void initLogger() {
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);
	}

	@BeforeEach
	void setUp() {
		Interception mockSettings = createMockSettings();
		// Use lenient stubbing since not all tests use the settings
		lenient().when(settingsProvider.get()).thenReturn(mockSettings);

		regexMatchingService = new RegexMatchingService(
				registry,
				translationService,
				settingsProvider,
				reloadableRegistry
		);
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(regexMatchingService);
	}

	@Test
	void shouldClearPatternIndexOnReload() {
		// Setup: registry returns empty pattern list
		when(registry.getKeys()).thenReturn(Collections.emptySet());

		// Build pattern index by calling match
		regexMatchingService.match("test", Locale.ENGLISH);

		// Reload clears the pattern index
		regexMatchingService.reload();

		// Next match should rebuild pattern index (verifying no exception from stale state)
		assertDoesNotThrow(() -> regexMatchingService.match("test2", Locale.ENGLISH));

		// Verify pattern index was rebuilt by checking registry was queried after reload
		verify(registry, atLeast(2)).getKeys();
	}

	@Test
	void shouldClearCacheOnReload() {
		// Setup settings with caching enabled
		Interception settings = createMockSettings();
		when(settings.getRegex().isCacheResults()).thenReturn(true);
		when(settingsProvider.get()).thenReturn(settings);
		when(registry.getKeys()).thenReturn(Collections.emptySet());

		RegexMatchingService serviceWithCache = new RegexMatchingService(
				registry,
				translationService,
				settingsProvider,
				reloadableRegistry
		);

		// Populate cache
		serviceWithCache.match("cached", Locale.ENGLISH);

		// Reload clears cache
		serviceWithCache.reload();

		// Next match should work without using stale cache (verifying no exception)
		assertDoesNotThrow(() -> serviceWithCache.match("cached", Locale.ENGLISH));
	}

	@Test
	void shouldHandleReloadWithDisabledRegex() {
		Interception settings = createMockSettings();
		when(settings.getRegex().isEnabled()).thenReturn(false);
		when(settingsProvider.get()).thenReturn(settings);

		RegexMatchingService disabledService = new RegexMatchingService(
				registry,
				translationService,
				settingsProvider,
				reloadableRegistry
		);

		// Reload should work even when regex is disabled
		assertDoesNotThrow(disabledService::reload);

		// Match should return empty when disabled
		Optional<String> result = disabledService.match("test", Locale.ENGLISH);
		assertTrue(result.isEmpty());
	}

	private Interception createMockSettings() {
		Interception settings = mock(Interception.class);
		Interception.RegexSettings regex = mock(Interception.RegexSettings.class);

		// Use lenient stubbing since not all tests need all settings
		lenient().when(settings.getRegex()).thenReturn(regex);
		lenient().when(regex.isEnabled()).thenReturn(true);

		return settings;
	}
}

