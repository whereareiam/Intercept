package me.whereareiam.intercept.common.messaging.integration;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.DefaultMessageService;
import me.whereareiam.intercept.common.messaging.loader.MessageFileData;
import me.whereareiam.intercept.common.messaging.loader.MessageFileLoader;
import me.whereareiam.intercept.common.messaging.loader.MessageFileScanner;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.common.messaging.regex.RegexMatchingService;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.messaging.regex.CompiledRegexPattern;
import me.whereareiam.intercept.model.config.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.google.inject.Provider;
import me.whereareiam.intercept.Registry;
import me.whereareiam.intercept.Reloadable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for regex matching system.
 * Tests loading patterns from YAML files and matching text against them.
 */
class RegexIntegrationTest {
	private DefaultMessageRegistry registry;
	private RegexMatchingService regexService;

	@BeforeEach
	void setUp() throws URISyntaxException {
		// Setup registry
		Registry<Reloadable> mockRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(mockRegistry);

		// Setup settings with regex enabled
		Settings settings = new SettingsTemplate().supply(new Settings());
		settings.getPerformance().getRegex().setEnabled(true);
		settings.getPerformance().getRegex().setCacheResults(false);

		// Set up YAML as default format for tests
		Config.setReader(Config.reader(Format.YAML));

		// Load test message files
		loadTestMessages();

		// Create message service
		MessageService messageService = new DefaultMessageService(registry, settings);

		// Create regex matching service with Provider
		Provider<Settings> settingsProvider = () -> settings;
		regexService = new RegexMatchingService(registry, messageService, settingsProvider, mockRegistry);
	}

	private void loadTestMessages() throws URISyntaxException {
		// Get test resources directory
		Path messagesRoot = Paths.get(getClass().getResource("/messages").toURI());

		// Scan for message files
		MessageFileScanner scanner = new MessageFileScanner(Format.YAML);
		List<Path> files = scanner.scanDirectory(messagesRoot);

		// Load each file
		TextProcessor textProcessor = new TextProcessor();
		MessageFileLoader loader = new MessageFileLoader(textProcessor, registry);

		for (Path file : files) {
			String keyPrefix = scanner.buildKeyPrefix(messagesRoot, file);
			MessageFileData fileData = Config.load(file, MessageFileData.class);
			loader.loadFromData(keyPrefix, fileData);
		}
	}

	@Test
	void shouldLoadRegexPatternsFromYAML() {
		MessageEntry entry = registry.get("regex.patterns.permission-error");
		assertNotNull(entry);
		assertTrue(entry.hasRegexPatterns());
		assertEquals(2, entry.getRegexPatterns().size());

		// Check first pattern
		CompiledRegexPattern pattern1 = entry.getRegexPatterns().get(0);
		assertEquals(10, pattern1.getPriority());
		assertNotNull(pattern1.getPattern());

		// Check second pattern
		CompiledRegexPattern pattern2 = entry.getRegexPatterns().get(1);
		assertEquals(5, pattern2.getPriority());
	}

	@Test
	void shouldMatchPermissionError() {
		String text = "You don't have permission: worldedit.region";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("You lack permission: worldedit.region", result.get());
	}

	@Test
	void shouldMatchCaseInsensitivePermissionError() {
		String text = "insufficient permission: worldedit.build";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("You lack permission: worldedit.build", result.get());
	}

	@Test
	void shouldMatchPlayerJoined() {
		String text = "Player Steve joined the game";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("Welcome, Steve!", result.get());
	}

	@Test
	void shouldMatchAlternativePlayerJoined() {
		String text = "Alex has joined";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("Welcome, Alex!", result.get());
	}

	@Test
	void shouldReturnEmptyWhenNoMatch() {
		String text = "This is some random text";
		Optional<String> result = regexService.match(text, Locale.US);

		assertFalse(result.isPresent());
	}

	@Test
	void shouldMatchHigherPriorityFirst() {
		// Test that when multiple patterns from different entries can match the same text,
		// the pattern with higher priority is matched first
		// "test hello" matches:
		// - priority-test with "test (\\w+)" at priority 20
		// - priority-test-low with "test (\\w+)" at priority 5
		// Should resolve to priority-test (priority 20)
		String text = "test hello";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("High priority: hello", result.get());
	}

	@Test
	void shouldHandleGermanLocale() {
		String text = "You don't have permission: worldedit.region";
		Optional<String> result = regexService.match(text, Locale.GERMANY);

		assertTrue(result.isPresent());
		assertEquals("Keine Berechtigung: worldedit.region", result.get());
	}
}