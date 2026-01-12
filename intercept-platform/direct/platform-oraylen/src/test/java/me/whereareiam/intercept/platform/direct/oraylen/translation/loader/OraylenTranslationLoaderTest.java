package me.whereareiam.intercept.platform.direct.oraylen.translation.loader;

import com.google.inject.Provider;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.common.persistence.format.DefaultTranslationFormatRegistry;
import me.whereareiam.intercept.common.persistence.format.type.locale.LocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.multilocale.MultiLocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.template.TemplateFormat;
import me.whereareiam.intercept.common.registry.DefaultReservedKeyRegistry;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
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

import static org.junit.jupiter.api.Assertions.*;

class OraylenTranslationLoaderTest {
	private OraylenTranslationLoader loader;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		Provider<Locale> localeProvider = () -> Locale.ENGLISH;
		MessageFormatRegistry formatRegistry = new DefaultTranslationFormatRegistry();
		formatRegistry.register(new LocaleFormat(), true);
		formatRegistry.register(new MultiLocaleFormat(), false);
		formatRegistry.register(new TemplateFormat(), false);
		ReservedKeyRegistry reservedKeyRegistry = new DefaultReservedKeyRegistry();
		loader = new OraylenTranslationLoader(
				localeProvider,
				formatRegistry,
				reservedKeyRegistry
		);
	}

	@Test
	void loadExcludesConfiguredRootFromKey() throws Exception {
		Path file = tempDir.resolve("messages").resolve("base").resolve("en.yml");
		writeMap(file, Map.of("test", "value"));

		TranslationSource source = TranslationSource.builder()
				.source(builder -> builder.directory("messages").format(FileFormat.LOCALE))
				.build();

		List<MessageFileData> documents = loader.load(tempDir, source);

		assertFalse(documents.isEmpty());
		MessageFileData document = documents.getFirst();
		MessageFileData.Section baseSection = (MessageFileData.Section) document.getEntries().get("base");
		assertTrue(baseSection.getEntries().containsKey("test"));
		MessageFileData.Entry entry = (MessageFileData.Entry) baseSection.getEntries().get("test");
		MessageValue value = entry.getLocales().get("en");
		assertEquals("value", value instanceof MessageValue.Text text ? text.value() : null);
		assertFalse(document.getEntries().containsKey("messages"));
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
				.source(builder -> builder.directory("messages").format(FileFormat.LOCALE))
				.source(builder -> builder.directory("messages/admin").format(FileFormat.LOCALE))
				.build();

		List<MessageFileData> documents = loader.load(tempDir, source);

		// More specific source should be loaded first
		assertFalse(documents.isEmpty());
		MessageFileData firstDoc = documents.getFirst();
		MessageFileData.Entry entry = (MessageFileData.Entry) firstDoc.getEntries().get("overlap");
		MessageValue value = entry.getLocales().get("en");
		assertEquals("specific", value instanceof MessageValue.Text text ? text.value() : null);
	}

	private void writeMap(Path path, Map<String, Object> data) throws Exception {
		Files.createDirectories(path.getParent());
		Config.getDefaultWriter().write(path, data);
	}
}
