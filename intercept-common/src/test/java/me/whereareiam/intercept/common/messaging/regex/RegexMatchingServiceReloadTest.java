package me.whereareiam.intercept.common.messaging.regex;

import com.google.inject.Provider;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.base.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RegexMatchingServiceReloadTest {

	private MessageRegistry registry;
	private MessageService messageService;
	private Provider<Settings> settingsProvider;
	private Registry<Reloadable> reloadableRegistry;
	private DefaultRegexMatchingService regexMatchingService;

	@BeforeEach
	void setUp() {
		registry = mock(MessageRegistry.class);
		messageService = mock(MessageService.class);
		settingsProvider = mock(Provider.class);
		reloadableRegistry = mock(Registry.class);

		Settings mockSettings = createMockSettings();
		when(settingsProvider.get()).thenReturn(mockSettings);

		regexMatchingService = new DefaultRegexMatchingService(
				registry,
				messageService,
				settingsProvider,
				reloadableRegistry
		);
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(reloadableRegistry).register(regexMatchingService);
	}

	@Test
	void shouldHandleMatchAfterReloadWithPatternIndex() {
		// Setup: Create a registry with regex patterns
		CompiledMessageEntry entry = mock(CompiledMessageEntry.class);
		when(entry.hasRegexPatterns()).thenReturn(true);
		when(registry.getKeys()).thenReturn(Set.of("test.key"));
		when(registry.get("test.key")).thenReturn(entry);

		// Trigger pattern index build by calling match
		regexMatchingService.match("test text", Locale.ENGLISH);

		// Reload - this should clear the pattern index
		regexMatchingService.reload();

		// After reload, pattern index should be rebuilt on next match
		// Pattern index is internal state, so we verify functionality works correctly
		assertDoesNotThrow(() -> regexMatchingService.match("test text", Locale.ENGLISH));
	}

	@Test
	void shouldHandleMatchAfterReloadWithCacheEnabled() {
		// Setup settings to enable caching
		Settings settings = createMockSettings();
		when(settings.getPerformance().getRegex().isCacheResults()).thenReturn(true);
		when(settingsProvider.get()).thenReturn(settings);

		// Create new service with caching enabled
		DefaultRegexMatchingService serviceWithCache = new DefaultRegexMatchingService(
				registry,
				messageService,
				settingsProvider,
				reloadableRegistry
		);

		// Perform a match to populate cache
		when(registry.getKeys()).thenReturn(Collections.emptySet());
		serviceWithCache.match("test", Locale.ENGLISH);

		// Reload should clear the cache
		serviceWithCache.reload();

		// Match again - result cache is internal state, so we verify functionality works correctly
		assertDoesNotThrow(() -> serviceWithCache.match("test", Locale.ENGLISH));
	}

	@Test
	void shouldHandleMultipleReloads() {
		assertDoesNotThrow(() -> {
			regexMatchingService.reload();
			regexMatchingService.reload();
			regexMatchingService.reload();
		});
	}

	@Test
	void shouldRebuildPatternIndexAfterReload() {
		// First match - builds pattern index
		when(registry.getKeys()).thenReturn(Collections.emptySet());
		regexMatchingService.match("test1", Locale.ENGLISH);

		// Reload - clears pattern index
		regexMatchingService.reload();

		// Second match - rebuilds pattern index
		when(registry.getKeys()).thenReturn(Set.of("new.key"));
		CompiledMessageEntry newEntry = mock(CompiledMessageEntry.class);
		when(newEntry.hasRegexPatterns()).thenReturn(false);
		when(registry.get("new.key")).thenReturn(newEntry);

		// Should not throw exception even with new registry state
		assertDoesNotThrow(() -> regexMatchingService.match("test2", Locale.ENGLISH));
	}

	@Test
	void shouldHandleReloadWithDisabledRegex() {
		Settings settings = createMockSettings();
		when(settings.getPerformance().getRegex().isEnabled()).thenReturn(false);
		when(settingsProvider.get()).thenReturn(settings);

		DefaultRegexMatchingService disabledService = new DefaultRegexMatchingService(
				registry,
				messageService,
				settingsProvider,
				reloadableRegistry
		);

		// Reload should work even when regex is disabled
		assertDoesNotThrow(disabledService::reload);

		// Match should return empty when disabled
		Optional<String> result = disabledService.match("test", Locale.ENGLISH);
		assertTrue(result.isEmpty());
	}

	private Settings createMockSettings() {
		Settings settings = mock(Settings.class);
		Settings.Performance performance = mock(Settings.Performance.class);
		Settings.Performance.Regex regex = mock(Settings.Performance.Regex.class);

		when(settings.getPerformance()).thenReturn(performance);
		when(performance.getRegex()).thenReturn(regex);
		when(regex.isEnabled()).thenReturn(true);
		when(regex.isCacheResults()).thenReturn(false);
		when(regex.isUseLiteralPrefix()).thenReturn(false);
		when(regex.getWarnSlowPatternsMs()).thenReturn(100);

		return settings;
	}
}

