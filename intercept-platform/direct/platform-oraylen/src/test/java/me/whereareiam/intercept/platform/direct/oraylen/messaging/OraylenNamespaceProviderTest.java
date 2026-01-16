package me.whereareiam.intercept.platform.direct.oraylen.messaging;

import me.whereareiam.intercept.common.persistence.file.DefaultTranslationFileCodecRegistry;
import me.whereareiam.intercept.common.persistence.file.DefaultTranslationFileCodecResolver;
import me.whereareiam.intercept.common.persistence.file.codec.YamlTranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenNamespaceProvider;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenTranslationRegistry;
import net.oraylen.api.Namespace;
import net.oraylen.api.translation.TranslationSource;
import net.oraylen.api.type.FileFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OraylenNamespaceProviderTest {
	@TempDir
	Path tempDir;

	@Test
	void resolvesNamespaceRootFromMostSpecificSource() {
		OraylenTranslationRegistry registry = new OraylenTranslationRegistry();
		TranslationSource source = TranslationSource.builder()
				.source(builder -> builder.directory("messages").format(FileFormat.LOCALE))
				.source(builder -> builder.directory("messages/admin").format(FileFormat.LOCALE))
				.build();

		Namespace namespace = Namespace.plugin("alpha");
		registry.registerNamespace(namespace, tempDir, source, Set.of("alpha:key"));

		TranslationFileCodecRegistry codecRegistry = new DefaultTranslationFileCodecRegistry();
		codecRegistry.register(new YamlTranslationFileCodec(), null, true);
		TranslationFileCodecResolver codecResolver = new DefaultTranslationFileCodecResolver(codecRegistry);
		OraylenNamespaceProvider provider = new OraylenNamespaceProvider(
				registry,
				() -> Locale.ENGLISH,
				codecRegistry,
				codecResolver
		);
		Map<String, Path> roots = provider.getNamespacePaths(tempDir);

		Path expected = tempDir.resolve("messages").resolve("admin").normalize();
		assertEquals(expected, roots.get(namespace.value()).normalize());
		assertTrue(provider.getRuntimeNamespaces().contains(namespace.value()));
	}
}
