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
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
class FileLoadingIntegrationTest {
	private DefaultMessageRegistry registry;
	private MessageService service;
	private MessageFileScanner scanner;
	private MessageFileLoader loader;
	private Path messagesRoot;

	@BeforeEach
	void setUp() throws URISyntaxException {
		registry = new DefaultMessageRegistry();
		Settings settings = new SettingsTemplate().supply(new Settings());
		service = new DefaultMessageService(registry, settings);

		// Set up YAML as default format for tests
		Config.setReader(Config.reader(Format.YAML));

		scanner = new MessageFileScanner(Format.YAML);
		loader = new MessageFileLoader(new TextProcessor(), registry);

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

		MessageFileData data = Config.load(colorsFile, MessageFileData.class);
		loader.loadFromData(keyPrefix, data);

		assertTrue(registry.exists("common.colors.primary"));
		assertTrue(registry.exists("common.colors.error"));
		assertEquals("<#5DADE2>", registry.get("common.colors.primary").getText());
	}

	@Test
	void shouldLoadYamlStyleTemplates() {
		// Load colors first (dependency)
		loadFile(messagesRoot.resolve("common/colors.yml"));

		// Load styles
		loadFile(messagesRoot.resolve("common/styles.yml"));

		assertTrue(registry.exists("common.styles.prefix"));
		assertTrue(registry.exists("common.styles.error-format"));
		assertTrue(registry.exists("common.styles.error-box"));
	}

	@Test
	void shouldLoadMultiLanguageMessages() {
		loadFile(messagesRoot.resolve("common/colors.yml"));
		loadFile(messagesRoot.resolve("common/styles.yml"));
		loadFile(messagesRoot.resolve("errors/permissions.yml"));

		assertTrue(registry.exists("errors.permissions.no-permission"));

		String enText = registry.get("errors.permissions.no-permission").getText("en_US");
		String deText = registry.get("errors.permissions.no-permission").getText("de_DE");

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
		assertTrue(result.contains("╔"));
		assertTrue(result.contains("╚"));
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

		// Read with Configura - Jackson handles deserialization automatically
		MessageFileData data = Config.load(file, MessageFileData.class);

		loader.loadFromData(keyPrefix, data);
	}
}