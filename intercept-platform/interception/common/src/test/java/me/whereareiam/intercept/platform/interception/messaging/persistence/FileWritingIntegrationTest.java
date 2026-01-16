package me.whereareiam.intercept.platform.interception.messaging.persistence;

import com.google.inject.Provider;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.node.ArrayNode;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.configura.node.StringNode;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.config.template.SettingsTemplate;
import me.whereareiam.intercept.common.persistence.DefaultTranslationFileWriter;
import me.whereareiam.intercept.common.persistence.file.DefaultTranslationFileCodecRegistry;
import me.whereareiam.intercept.common.persistence.file.DefaultTranslationFileCodecResolver;
import me.whereareiam.intercept.common.persistence.file.codec.YamlTranslationFileCodec;
import me.whereareiam.intercept.common.persistence.format.DefaultTranslationFormatRegistry;
import me.whereareiam.intercept.common.persistence.format.type.multilocale.MultiLocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.template.TemplateFormat;
import me.whereareiam.intercept.common.provider.DefaultPlatformNamespaceProvider;
import me.whereareiam.intercept.common.registry.DefaultReservedKeyRegistry;
import me.whereareiam.intercept.common.translation.namespace.DefaultNamespaceResolver;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.file.*;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.intercept.platform.interception.messaging.format.InterceptionKeyHandler;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for writing message documents back to disk.
 */
class FileWritingIntegrationTest {
	private static final MessageExtensionKey<MapMessageExtensionPayload> INTERCEPTION_KEY = new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);

	@TempDir
	Path tempDir;

	private MessageFileWriter writer;

	@BeforeEach
	void setUp() {
		Config.setWriter(Config.writer(Format.YAML));
		Config.setReader(Config.reader(Format.YAML));
		MessageFormatRegistry formatRegistry = new DefaultTranslationFormatRegistry();
		formatRegistry.register(new MultiLocaleFormat(), true);
		formatRegistry.register(new TemplateFormat(), false);
		ReservedKeyRegistry reservedKeyRegistry = new DefaultReservedKeyRegistry();
		reservedKeyRegistry.register(new InterceptionKeyHandler());
		Settings settings = new SettingsTemplate().supply(new Settings());
		Provider<Settings> settingsProvider = () -> settings;
		NamespaceResolver namespaceResolver = new DefaultNamespaceResolver(
				settingsProvider,
				new DefaultPlatformNamespaceProvider(),
				tempDir
		);
		TranslationFileCodecRegistry codecRegistry = new DefaultTranslationFileCodecRegistry();
		codecRegistry.register(new YamlTranslationFileCodec(), null, true);
		TranslationFileCodecResolver codecResolver = new DefaultTranslationFileCodecResolver(codecRegistry);
		writer = new DefaultTranslationFileWriter(
				formatRegistry,
				reservedKeyRegistry,
				namespaceResolver,
				() -> Locale.ENGLISH,
				codecRegistry,
				codecResolver
		);
	}

	@Test
	void shouldWriteDocumentWithNestedPath() {
		MessageFileData document = new MessageFileData();

		MessageFileData.Entry entry = new MessageFileData.Entry();
		entry.setText(MessageValue.text("Welcome!"));

		Map<String, Object> regex = new LinkedHashMap<>();
		regex.put("pattern", ".*hello.*");
		regex.put("priority", 5);
		regex.put("replaceMatched", true);
		regex.put("placeholders", Map.of("name", "$1"));

		MapMessageExtensionPayload interception = new MapMessageExtensionPayload(
				"interception",
				Map.of("patterns", List.of(regex))
		);
		MessageExtensions extensions = new MessageExtensions();
		extensions.put(INTERCEPTION_KEY, interception);
		entry.setExtensions(extensions);

		document.putEntry("welcome", entry);

		String relativePath = "errors/permissions";
		writer.write(relativePath, document, "MULTI_LOCALE");

		Path writtenPath = writer.resolvePath(relativePath);
		assertTrue(Files.exists(writtenPath), "Expected file to be written to disk");

		Node loaded = Config.loadNode(writtenPath);
		ObjectNode root = loaded instanceof ObjectNode objectNode ? objectNode : new ObjectNode();
		assertTrue(root.getValues().containsKey("welcome"));
		Node loadedEntry = root.getValues().get("welcome");
		assertInstanceOf(ObjectNode.class, loadedEntry);
		ObjectNode loadedMap = (ObjectNode) loadedEntry;
		Node textNode = loadedMap.getValues().get("text");
		assertInstanceOf(StringNode.class, textNode);
		assertEquals("Welcome!", ((StringNode) textNode).getValue());
		Node interceptionRaw = loadedMap.getValues().get("interception");
		assertInstanceOf(ObjectNode.class, interceptionRaw);
		ObjectNode interceptionMap = (ObjectNode) interceptionRaw;
		Node patternsRaw = interceptionMap.getValues().get("patterns");
		assertInstanceOf(ArrayNode.class, patternsRaw);
		assertEquals(1, ((ArrayNode) patternsRaw).getValues().size());
	}
}
