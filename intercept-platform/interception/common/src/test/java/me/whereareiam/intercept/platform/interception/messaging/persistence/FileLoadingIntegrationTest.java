package me.whereareiam.intercept.platform.interception.messaging.persistence;

import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.messaging.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.registry.InterceptTranslationRegistry;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.platform.interception.SemanticaTestHelper;
import me.whereareiam.intercept.platform.interception.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that load real YAML files from test resources.
 */
@ExtendWith(MockitoExtension.class)
class FileLoadingIntegrationTest {
	@Mock
	private Registry<Reloadable> reloadableRegistry;
	
	private DefaultMessageRegistry registry;
	private TranslationService<Locale> service;
	private MessageFileScanner scanner;
	private MessageFileLoader loader;
	private Path messagesRoot;

	@BeforeEach
	void setUp() throws URISyntaxException {
		InterceptTranslationRegistry translationRegistry = new InterceptTranslationRegistry();
		registry = new DefaultMessageRegistry(translationRegistry, reloadableRegistry);
		Settings settings = new SettingsTemplate().supply(new Settings());
		service = SemanticaTestHelper.createService(settings, translationRegistry);

		// Set up YAML as default format for tests
		Config.setReader(Config.reader(Format.YAML));
		Config.registerAdapter(MessageDocument.Node.class, new MessageDocumentNodeAdapter());

		scanner = new MessageFileScanner(Format.YAML);
		loader = new DefaultMessageFileLoader(
				new TextProcessor(),
				() -> Locale.US
		);

		// Get path to test resources
		messagesRoot = Paths.get(getClass().getResource("/messages").toURI());
	}

	@Test
	void shouldLoadAllFilesFromDirectory() {
		List<Path> files = scanner.scanDirectory(messagesRoot);

		// Should find colors.yml, styles.yml, permissions.yml, myplugin.yml (only YAML files)
		assertTrue(files.size() >= 4, "Should find at least 4 YAML message files");
	}

	@Test
	void shouldLoadYamlColorPalette() {
		Path colorsFile = messagesRoot.resolve("common/colors.yml");
		String keyPrefix = scanner.buildKeyPrefix(messagesRoot, colorsFile);

		MessageDocument data = Config.load(colorsFile, MessageDocument.class);
		registerEntries(loader.loadFromData(keyPrefix, data));

		assertTrue(registry.exists("common.colors.primary"));
		assertTrue(registry.exists("common.colors.error"));
		String text = service.resolve("common.colors.primary", Locale.US);
		assertEquals("<#5DADE2>", text);
	}

	@Test
	void shouldLoadYamlStyleTemplates() {
		// Load colors first (dependency)
		loadFile(messagesRoot.resolve("common/colors.yml"));

		// Load styles
		loadFile(messagesRoot.resolve("common/styles.yml"));

		assertTrue(registry.exists("common.styles.prefix"));
		assertTrue(registry.exists("common.styles.error.format"));
		assertTrue(registry.exists("common.styles.error.box"));
	}

	@Test
	void shouldLoadMultiLanguageMessages() {
		loadFile(messagesRoot.resolve("common/colors.yml"));
		loadFile(messagesRoot.resolve("common/styles.yml"));
		loadFile(messagesRoot.resolve("errors/permissions.yml"));

		assertTrue(registry.exists("errors.permissions.no-permission"));

		String enText = service.resolve("errors.permissions.no-permission", Locale.US,
				Map.of("permission", "intercept.admin"));
		String deText = service.resolve("errors.permissions.no-permission", Locale.GERMANY,
				Map.of("permission", "intercept.admin"));

		assertNotNull(enText);
		assertNotNull(deText);
		assertTrue(enText.contains("permission"));
		assertTrue(deText.contains("Berechtigung"));
	}

	@Test
	void shouldResolveMessageFromLoadedFiles() {
		loadAllFiles();

		String result = service.resolve(
				"errors.permissions.no-permission",
				Locale.US,
				Map.of("permission", "intercept.admin")
		);

		assertNotNull(result);
		assertTrue(result.contains("intercept.admin"));
		assertTrue(result.contains("permission"));
	}

	@Test
	void shouldResolveMultiLineMessage() {
		loadAllFiles();

		String result = service.resolve(
				"errors.permissions.rank-required",
				Locale.US,
				Map.of("rank", "ADMIN")
		);

		assertNotNull(result);
		assertTrue(result.contains("ADMIN"));
		assertTrue(result.contains("╔"), () -> "Expected ╔ in box output, got: " + result);
		assertTrue(result.contains("╚"), () -> "Expected ╚ in box output, got: " + result);
	}

	@Test
	void shouldResolveNestedTemplatesAndReferences() {
		loadAllFiles();

		String result = service.resolve(
				"plugins.myplugin.debug",
				Locale.US,
				Map.of("info", "test data")
		);

		assertNotNull(result);
		assertTrue(result.contains("test data"));
	}

	@Test
	void shouldResolveGermanTranslation() {
		loadAllFiles();

		String result = service.resolve(
				"errors.permissions.no-permission",
				Locale.GERMANY,
				Map.of("permission", "intercept.admin")
		);

		assertNotNull(result);
		assertTrue(result.contains("Berechtigung"));
		assertTrue(result.contains("intercept.admin"));
	}

	// Helper methods

	private void loadAllFiles() {
		List<Path> files = scanner.scanDirectory(messagesRoot);
		for (Path file : files) {
			loadFile(file);
		}
	}

	private void loadFile(Path file) {
		String keyPrefix = scanner.buildKeyPrefix(messagesRoot, file);

		MessageDocument data = Config.load(file, MessageDocument.class);
		registerEntries(loader.loadFromData(keyPrefix, data));
	}

	private void registerEntries(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return;
		service.register(entries);
	}
}





