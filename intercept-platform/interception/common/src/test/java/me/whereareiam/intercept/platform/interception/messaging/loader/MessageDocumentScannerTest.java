package me.whereareiam.intercept.platform.interception.messaging.loader;

import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.persistence.TranslationFileScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDocumentScannerTest {
	private TranslationFileScanner yamlScanner;
	private TranslationFileScanner jsonScanner;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		yamlScanner = new TranslationFileScanner(Format.YAML);
		jsonScanner = new TranslationFileScanner(Format.JSON);
	}

	@Test
	void shouldFindYamlFiles() throws IOException {
		// Create test files
		Files.createFile(tempDir.resolve("test.yml"));
		Files.createFile(tempDir.resolve("test.txt")); // Should be ignored

		List<Path> files = yamlScanner.scanDirectory(tempDir);

		assertEquals(1, files.size());
		assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("test.yml")));
	}

	@Test
	void shouldFindJsonFiles() throws IOException {
		Files.createFile(tempDir.resolve("test.json"));
		Files.createFile(tempDir.resolve("test.js")); // Should be ignored

		List<Path> files = jsonScanner.scanDirectory(tempDir);

		assertEquals(1, files.size());
		assertEquals("test.json", files.get(0).getFileName().toString());
	}

	@Test
	void shouldScanNestedDirectories() throws IOException {
		Path nested = tempDir.resolve("errors").resolve("permissions");
		Files.createDirectories(nested);

		Files.createFile(tempDir.resolve("common.yml"));
		Files.createFile(tempDir.resolve("errors").resolve("general.yml"));
		Files.createFile(nested.resolve("admin.yml"));

		List<Path> files = yamlScanner.scanDirectory(tempDir);

		assertEquals(3, files.size());
	}

	@Test
	void shouldBuildKeyFromRootFile() {
		Path root = Path.of("/messages");
		Path file = Path.of("/messages/common.yml");

		String key = yamlScanner.buildKeyPrefix(root, file);

		assertEquals("common", key);
	}

	@Test
	void shouldBuildKeyFromNestedFile() {
		Path root = Path.of("/messages");
		Path file = Path.of("/messages/errors/permissions.yml");

		String key = yamlScanner.buildKeyPrefix(root, file);

		assertEquals("errors.permissions", key);
	}

	@Test
	void shouldBuildKeyFromDeeplyNestedFile() {
		Path root = Path.of("/messages");
		Path file = Path.of("/messages/errors/database/connection.yml");

		String key = yamlScanner.buildKeyPrefix(root, file);

		assertEquals("errors.database.connection", key);
	}

	@Test
	void shouldHandleFileWithNoExtension() {
		Path root = Path.of("/messages");
		Path file = Path.of("/messages/test");

		String key = yamlScanner.buildKeyPrefix(root, file);

		assertEquals("test", key);
	}

	@Test
	void shouldHandleMultipleDotsInFilename() {
		Path root = Path.of("/messages");
		Path file = Path.of("/messages/test.en.yml");

		String key = yamlScanner.buildKeyPrefix(root, file);

		// Should remove only the extension, not the locale part
		assertEquals("test.en", key);
	}

	@Test
	void shouldHandleEmptyDirectory() {
		List<Path> files = yamlScanner.scanDirectory(tempDir);

		assertTrue(files.isEmpty());
	}

	@Test
	void shouldIgnoreHiddenFiles() throws IOException {
		// Note: on Windows, we'd need to set attributes, on Unix files starting with . are hidden
		Files.createFile(tempDir.resolve(".hidden.yml"));
		Files.createFile(tempDir.resolve("visible.yml"));

		List<Path> files = yamlScanner.scanDirectory(tempDir);

		// Should only find visible.yml
		assertEquals(1, files.size());
		assertEquals("visible.yml", files.get(0).getFileName().toString());
	}
}
