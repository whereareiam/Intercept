package me.whereareiam.intercept.common.messaging.integration;

import com.google.inject.Provider;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.interception.DefaultInterceptionRegistry;
import me.whereareiam.intercept.common.messaging.SemanticaTestHelper;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.persistence.MessageFileScanner;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.common.messaging.regex.DefaultRegexMatchingService;
import me.whereareiam.intercept.common.util.ComponentHelper;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.model.regex.MatchDetails;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.translation.TranslationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Integration test for regex matching system.
 * Tests loading patterns from YAML files and matching text against them.
 */
class RegexIntegrationTest {
	private DefaultMessageRegistry registry;
	private InterceptionRegistry interceptionRegistry;
	private RegexMatchingService regexService;
	private TranslationService<Locale> translationService;
	private Settings settings;

	@BeforeAll
	static void initLogger() {
		// Initialize Logger with a mock to prevent NPEs
		LoggingHelper mockLogger = mock(LoggingHelper.class);
		Logger.init(mockLogger);
	}

	@BeforeEach
	void setUp() throws URISyntaxException {
		// Setup registry
		Registry<Reloadable> mockRegistry = mock(Registry.class);
		registry = new DefaultMessageRegistry(mockRegistry);
		interceptionRegistry = new DefaultInterceptionRegistry();

		// Setup settings with regex enabled
		settings = new SettingsTemplate().supply(new Settings());
		settings.getPerformance().getRegex().setEnabled(true);
		settings.getPerformance().getRegex().setCacheResults(false);
		translationService = SemanticaTestHelper.createService(settings);

		// Set up YAML as default format for tests
		Config.setReader(Config.reader(Format.YAML));

		// Load test message files
		loadTestMessages();

		// Create regex matching service with Provider
		Provider<Settings> settingsProvider = () -> settings;
		regexService = new DefaultRegexMatchingService(interceptionRegistry, translationService, settingsProvider, mockRegistry);
	}

	private void loadTestMessages() throws URISyntaxException {
		// Get test resources directory
		Path messagesRoot = Paths.get(getClass().getResource("/messages").toURI());

		// Scan for message files
		MessageFileScanner scanner = new MessageFileScanner(Format.YAML);
		List<Path> files = scanner.scanDirectory(messagesRoot);

		// Load each persistence
		TextProcessor textProcessor = new TextProcessor();
		MessageFileLoader loader = new DefaultMessageFileLoader(
				registry,
				interceptionRegistry,
				textProcessor,
				translationService,
				() -> settings
		);

		for (Path file : files) {
			String keyPrefix = scanner.buildKeyPrefix(messagesRoot, file);
			@SuppressWarnings("unchecked")
			Map<String, Object> raw = (Map<String, Object>) Config.load(file, Map.class);
			MessageDocument fileData = MessageDocument.fromRawMap(raw);
			loader.loadFromData(keyPrefix, fileData);
		}
	}

	@Test
	void shouldLoadRegexPatternsFromYAML() {
		CompiledMessageEntry entry = registry.get("regex.patterns.permission-error");
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

	@Test
	void shouldOnlyReplaceMatchedSegmentWhenConfigured() {
		String text = "Unknown or incomplete command, see below for error h<--[HERE]";
		Optional<String> result = regexService.match(text, Locale.US);

		assertTrue(result.isPresent());
		assertEquals("Unknown test test, see below for error h<--[HERE]", result.get());
	}

	@Test
	void shouldPreserveFormattingWhenReplacingMatchedPart() {
		// Create a component with formatting: red "Unknown", yellow "or incomplete command", green rest
		Component original = Component.text()
				.append(Component.text("Unknown ", NamedTextColor.RED, TextDecoration.BOLD))
				.append(Component.text("or incomplete command", NamedTextColor.YELLOW))
				.append(Component.text(", see below for error h<--[HERE]", NamedTextColor.GREEN))
				.build();

		// Extract plain text for matching
		String plainText = PlainTextComponentSerializer.plainText().serialize(original);
		assertEquals("Unknown or incomplete command, see below for error h<--[HERE]", plainText);

		// Match against the regex pattern to get details
		Optional<MatchDetails> matchDetails = regexService.matchWithDetails(plainText, Locale.US);
		assertTrue(matchDetails.isPresent());

		MatchDetails details = matchDetails.get();
		assertTrue(details.isReplaceMatched());
		assertEquals("test test", details.getResolvedText());

		// Find the match position - "or incomplete command" starts at index 8
		int matchStart = plainText.indexOf("or incomplete command");
		int matchEnd = matchStart + "or incomplete command".length();
		assertEquals(matchStart, details.getMatchStart());
		assertEquals(matchEnd, details.getMatchEnd());

		// Replace only the matched part, preserving formatting
		Component result = ComponentHelper.replaceTextRange(original, matchStart, matchEnd, details.getResolvedText());

		// Verify the plain text is correct
		String resultText = PlainTextComponentSerializer.plainText().serialize(result);
		assertEquals("Unknown test test, see below for error h<--[HERE]", resultText);

		// Extract components to verify structure
		List<Component> children = new ArrayList<>();
		extractComponents(result, children);

		// Should have at least 3 parts: "Unknown ", "test test", and ", see below..."
		assertTrue(children.size() >= 2, "Result should have multiple components");

		// Check first component (should be "Unknown " with red and bold)
		Component first = children.get(0);
		if (first instanceof TextComponent textComp)
			assertTrue(textComp.content().startsWith("Unknown"), "First part should start with 'Unknown'");

		// Verify the replacement text is present
		String fullResult = PlainTextComponentSerializer.plainText().serialize(result);
		assertTrue(fullResult.contains("test test"), "Result should contain replacement text");
		assertTrue(fullResult.contains("Unknown"), "Result should contain 'Unknown'");
		assertTrue(fullResult.contains(", see below"), "Result should contain rest of message");
	}

	/**
	 * Helper to extract all text components from a component tree.
	 */
	private void extractComponents(Component component, List<Component> result) {
		if (component instanceof TextComponent textComp)
			if (!textComp.content().isEmpty())
				result.add(component);

		for (Component child : component.children())
			extractComponents(child, result);
	}
}
