package me.whereareiam.intercept.platform.direct.oraylen.translation.loader;

import com.google.inject.Provider;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationEntry;
import me.whereareiam.intercept.platform.direct.oraylen.messaging.loader.OraylenTranslationLoader;
import me.whereareiam.semantica.model.TextValue;
import net.oraylen.api.translation.FileFormat;
import net.oraylen.api.translation.TranslationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OraylenTranslationLoaderTest {
	@TempDir
	Path tempDir;

	private OraylenTranslationLoader loader;

	@BeforeEach
	void setUp() {
		Provider<Locale> localeProvider = () -> Locale.ENGLISH;
		loader = new OraylenTranslationLoader(
				localeProvider
		);
	}

	@Test
	void loadExcludesConfiguredRootFromKey() throws Exception {
		Path file = tempDir.resolve("messages").resolve("base").resolve("en.yml");
		writeMap(file, Map.of("test", "value"));

		TranslationSource source = TranslationSource.builder()
				.required(builder -> builder.directory("messages").format(FileFormat.LOCALE))
				.build();

		List<TranslationDocument> documents = loader.load(tempDir, source);

		assertFalse(documents.isEmpty());
		TranslationDocument document = documents.getFirst();
		assertTrue(document.getEntries().containsKey("base.test"));
		TranslationEntry entry = document.getEntries().get("base.test");
		assertEquals(TranslationEntry.EntryType.LOCALIZED, entry.getType());
		Map<Locale, TextValue> values = entry.getLocalizedValues();
		assertEquals("value", values.get(Locale.ENGLISH).asString());
		assertFalse(document.getEntries().containsKey("messages.base.test"));
	}

	@Test
	void loadUsesMoreSpecificSourceFirst() throws Exception {
		writeMap(
				tempDir.resolve("messages").resolve("en.yml"),
				Map.of(
						"overlap", "general",
						"generalOnly", "general"
				)
		);
		writeMap(
				tempDir.resolve("messages").resolve("admin").resolve("en.yml"),
				Map.of(
						"overlap", "specific",
						"adminOnly", "specific"
				)
		);

		TranslationSource source = TranslationSource.builder()
				.required(builder -> builder.directory("messages").format(FileFormat.LOCALE))
				.required(builder -> builder.directory("messages/admin").format(FileFormat.LOCALE))
				.build();

		List<TranslationDocument> documents = loader.load(tempDir, source);

		// More specific source should be loaded first
		assertFalse(documents.isEmpty());
		TranslationDocument firstDoc = documents.getFirst();
		assertTrue(firstDoc.getEntries().containsKey("overlap"));
		assertEquals("specific", firstDoc.getEntries().get("overlap").getLocalizedValues().get(Locale.ENGLISH).asString());
	}

	private void writeMap(Path path, Map<String, Object> data) throws Exception {
		Files.createDirectories(path.getParent());
		Config.getDefaultWriter().write(path, data);
	}
}
