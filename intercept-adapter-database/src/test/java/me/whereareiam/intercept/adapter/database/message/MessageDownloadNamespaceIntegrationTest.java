package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageDownloadCoordinator;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.NamespaceUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageDownloadNamespaceIntegrationTest extends BaseTest {
	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void downloadUsesRuntimeNamespacesWhenEnabled(DatabaseType type) throws IOException {
		clearTables(type);
		Path messagesPath = Files.createTempDirectory("messages-download-test");
		MessageFileRepository fileRepository = getJdbi(type).onDemand(MessageFileRepository.class);
		MessageEntryRepository entryRepository = getJdbi(type).onDemand(MessageEntryRepository.class);
		MessageTranslationRepository translationRepository = getJdbi(type).onDemand(MessageTranslationRepository.class);
		MessageTemplateRepository templateRepository = getJdbi(type).onDemand(MessageTemplateRepository.class);
		MessageExtensionRepository extensionRepository = getJdbi(type).onDemand(MessageExtensionRepository.class);

		long interceptFile = insertFile(fileRepository, Constants.Namespace.INTERNAL, "core/messages");
		long sharedFile = insertFile(fileRepository, "shared", "shared/messages");
		long proxyFile = insertFile(fileRepository, "proxy", "proxy/messages");

		insertMessage(entryRepository, translationRepository, interceptFile, "hello", "Intercept");
		insertMessage(entryRepository, translationRepository, sharedFile, "hello", "Shared");
		insertMessage(entryRepository, translationRepository, proxyFile, "hello", "Proxy");

		Settings settings = new Settings();
		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setExtra(List.of("shared", "proxy"));
		namespaces.setLoad(List.of("shared"));
		Settings.Translation translation = new Settings.Translation();
		translation.setNamespaces(namespaces);
		settings.setTranslation(translation);

		PlatformNamespaceProvider namespaceProvider = new PlatformNamespaceProvider() {
			@Override
			public String getRuntimeNamespace() {
				return Constants.Namespace.INTERNAL;
			}

			@Override
			public boolean useRuntimeNamespacesForDownload() {
				return true;
			}
		};

		NamespaceResolver namespaceResolver = TestMessageSupport.createNamespaceResolver(
				settings,
				namespaceProvider,
				messagesPath
		);
		MessageFormatRegistry formatRegistry = TestMessageSupport.createFormatRegistry();
		RecordingMessageFileWriter writer = new RecordingMessageFileWriter(messagesPath);

		MessageDownloadCoordinator coordinator = new MessageDownloadCoordinator(
				fileRepository,
				entryRepository,
				translationRepository,
				templateRepository,
				extensionRepository,
				writer,
				formatRegistry,
				null,
				namespaceResolver,
				namespaceProvider
		);

		MessageSnapshot snapshot = coordinator.download();

		String interceptPrefix = buildKeyPrefix(Constants.Namespace.INTERNAL, "core/messages");
		String sharedPrefix = buildKeyPrefix("shared", "shared/messages");
		String proxyPrefix = buildKeyPrefix("proxy", "proxy/messages");

		assertTrue(snapshot.getFilePaths().containsKey(interceptPrefix));
		assertTrue(snapshot.getFilePaths().containsKey(sharedPrefix));
		assertFalse(snapshot.getFilePaths().containsKey(proxyPrefix));
		assertEquals(2, writer.calls().size());

		assertTrue(snapshot.getEntries().containsKey(interceptPrefix + ".hello"));
		assertTrue(snapshot.getEntries().containsKey(sharedPrefix + ".hello"));
		assertFalse(snapshot.getEntries().containsKey(proxyPrefix + ".hello"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void downloadSkipsDisallowedNamespaces(DatabaseType type) throws IOException {
		clearTables(type);
		Path messagesPath = Files.createTempDirectory("messages-download-skip-test");
		MessageFileRepository fileRepository = getJdbi(type).onDemand(MessageFileRepository.class);
		MessageEntryRepository entryRepository = getJdbi(type).onDemand(MessageEntryRepository.class);
		MessageTranslationRepository translationRepository = getJdbi(type).onDemand(MessageTranslationRepository.class);
		MessageTemplateRepository templateRepository = getJdbi(type).onDemand(MessageTemplateRepository.class);
		MessageExtensionRepository extensionRepository = getJdbi(type).onDemand(MessageExtensionRepository.class);

		long interceptFile = insertFile(fileRepository, Constants.Namespace.INTERNAL, "core/messages");
		long sharedFile = insertFile(fileRepository, "shared", "shared/messages");

		insertMessage(entryRepository, translationRepository, interceptFile, "hello", "Intercept");
		insertMessage(entryRepository, translationRepository, sharedFile, "hello", "Shared");

		Settings settings = new Settings();
		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setExtra(List.of("shared"));
		Settings.Translation translation = new Settings.Translation();
		translation.setNamespaces(namespaces);
		settings.setTranslation(translation);

		PlatformNamespaceProvider namespaceProvider = new PlatformNamespaceProvider() {
			@Override
			public String getRuntimeNamespace() {
				return Constants.Namespace.INTERNAL;
			}

			@Override
			public boolean useRuntimeNamespacesForDownload() {
				return true;
			}

			@Override
			public boolean isMessagesPathNamespaceAllowed(String namespace) {
				return namespace == null || !namespace.equalsIgnoreCase(Constants.Namespace.INTERNAL);
			}
		};

		NamespaceResolver namespaceResolver = TestMessageSupport.createNamespaceResolver(
				settings,
				namespaceProvider,
				messagesPath
		);
		MessageFormatRegistry formatRegistry = TestMessageSupport.createFormatRegistry();
		RecordingMessageFileWriter writer = new RecordingMessageFileWriter(messagesPath);

		MessageDownloadCoordinator coordinator = new MessageDownloadCoordinator(
				fileRepository,
				entryRepository,
				translationRepository,
				templateRepository,
				extensionRepository,
				writer,
				formatRegistry,
				null,
				namespaceResolver,
				namespaceProvider
		);

		MessageSnapshot snapshot = coordinator.download();

		String interceptPrefix = buildKeyPrefix(Constants.Namespace.INTERNAL, "core/messages");
		String sharedPrefix = buildKeyPrefix("shared", "shared/messages");

		assertFalse(snapshot.getFilePaths().containsKey(interceptPrefix));
		assertTrue(snapshot.getFilePaths().containsKey(sharedPrefix));
		assertEquals(1, writer.calls().size());

		assertFalse(snapshot.getEntries().containsKey(interceptPrefix + ".hello"));
		assertTrue(snapshot.getEntries().containsKey(sharedPrefix + ".hello"));
	}

	@ParameterizedTest
	@EnumSource(DatabaseType.class)
	void downloadUsesRuntimeRootsAndKeepsExtrasInInterceptMessages(DatabaseType type) throws IOException {
		clearTables(type);
		Path messagesPath = Files.createTempDirectory("messages-download-runtime-test");
		Path runtimeRoot = Files.createTempDirectory("messages-download-runtime-root");
		MessageFileRepository fileRepository = getJdbi(type).onDemand(MessageFileRepository.class);
		MessageEntryRepository entryRepository = getJdbi(type).onDemand(MessageEntryRepository.class);
		MessageTranslationRepository translationRepository = getJdbi(type).onDemand(MessageTranslationRepository.class);
		MessageTemplateRepository templateRepository = getJdbi(type).onDemand(MessageTemplateRepository.class);
		MessageExtensionRepository extensionRepository = getJdbi(type).onDemand(MessageExtensionRepository.class);

		String runtimeNamespace = "oraylen.plugin.alpha";
		long runtimeFile = insertFile(fileRepository, runtimeNamespace, "runtime/messages");
		long sharedFile = insertFile(fileRepository, "shared", "shared/messages");

		insertMessage(entryRepository, translationRepository, runtimeFile, "hello", "Runtime");
		insertMessage(entryRepository, translationRepository, sharedFile, "hello", "Shared");

		Settings settings = new Settings();
		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setExtra(List.of(runtimeNamespace, "shared"));
		Settings.Translation translation = new Settings.Translation();
		translation.setNamespaces(namespaces);
		settings.setTranslation(translation);

		PlatformNamespaceProvider namespaceProvider = new PlatformNamespaceProvider() {
			@Override
			public String getRuntimeNamespace() {
				return runtimeNamespace;
			}

			@Override
			public Set<String> getRuntimeNamespaces() {
				return Set.of(runtimeNamespace);
			}

			@Override
			public Map<String, Path> getNamespacePaths(Path messagesRoot) {
				return Map.of(runtimeNamespace, runtimeRoot);
			}
		};

		NamespaceResolver namespaceResolver = TestMessageSupport.createNamespaceResolver(
				settings,
				namespaceProvider,
				messagesPath
		);
		MessageFormatRegistry formatRegistry = TestMessageSupport.createFormatRegistry();
		NamespacePathRecordingWriter writer = new NamespacePathRecordingWriter(
				messagesPath,
				namespaceResolver
		);

		MessageDownloadCoordinator coordinator = new MessageDownloadCoordinator(
				fileRepository,
				entryRepository,
				translationRepository,
				templateRepository,
				extensionRepository,
				writer,
				formatRegistry,
				null,
				namespaceResolver,
				namespaceProvider
		);

		MessageSnapshot snapshot = coordinator.download();

		String runtimePrefix = buildKeyPrefix(runtimeNamespace, "runtime/messages");
		String sharedPrefix = buildKeyPrefix("shared", "shared/messages");

		Path runtimePath = snapshot.getFilePaths().get(runtimePrefix);
		Path sharedPath = snapshot.getFilePaths().get(sharedPrefix);

		assertEquals(runtimeRoot.resolve("runtime/messages.yml").normalize(), runtimePath);
		assertEquals(messagesPath.resolve("shared").resolve("shared/messages.yml").normalize(), sharedPath);
	}

	private long insertFile(MessageFileRepository repository, String namespace, String filePath) {
		return repository.insert(namespace, filePath, "TEST");
	}

	private void insertMessage(
			MessageEntryRepository entryRepository,
			MessageTranslationRepository translationRepository,
			long fileId,
			String entryKey,
			String text
	) {
		long entryId = entryRepository.insert(fileId, entryKey, entryKey, MessageType.MESSAGE.name());
		translationRepository.insert(entryId, Locale.ENGLISH, text);
	}

	private String buildKeyPrefix(String namespace, String filePath) {
		String prefix = filePath.replace('\\', '.').replace('/', '.');
		return NamespaceUtil.qualify(namespace, prefix);
	}

	private void clearTables(DatabaseType type) {
		getJdbi(type).useHandle(handle -> {
			handle.execute("DELETE FROM intercept_message_extensions");
			handle.execute("DELETE FROM intercept_message_templates");
			handle.execute("DELETE FROM intercept_message_translations");
			handle.execute("DELETE FROM intercept_message_entries");
			handle.execute("DELETE FROM intercept_message_files");
		});
	}

	private static final class RecordingMessageFileWriter implements MessageFileWriter {
		private final Path messagesPath;
		private final List<WriteCall> calls = new ArrayList<>();

		private RecordingMessageFileWriter(Path messagesPath) {
			this.messagesPath = messagesPath;
		}

		@Override
		public void write(String namespace, String relativePath, MessageFileData fileData, String formatId) {
			calls.add(new WriteCall(namespace, relativePath, formatId));
		}

		@Override
		public void write(String relativePath, MessageFileData fileData) {
			calls.add(new WriteCall(null, relativePath, null));
		}

		@Override
		public Path resolvePath(String namespace, String relativePath) {
			Path root = namespace == null ? messagesPath : messagesPath.resolve(namespace);
			return root.resolve(relativePath + ".yml").normalize();
		}

		@Override
		public Path resolvePath(String relativePath) {
			return messagesPath.resolve(relativePath + ".yml").normalize();
		}

		public List<WriteCall> calls() {
			return List.copyOf(calls);
		}
	}

	private record WriteCall(String namespace, String relativePath, String formatId) {
	}

	private static final class NamespacePathRecordingWriter implements MessageFileWriter {
		private final Path messagesPath;
		private final NamespaceResolver namespaceResolver;
		private final List<WriteCall> calls = new ArrayList<>();

		private NamespacePathRecordingWriter(
				Path messagesPath,
				NamespaceResolver namespaceResolver
		) {
			this.messagesPath = messagesPath;
			this.namespaceResolver = namespaceResolver;
		}

		@Override
		public void write(String namespace, String relativePath, MessageFileData fileData, String formatId) {
			calls.add(new WriteCall(namespace, relativePath, formatId));
		}

		@Override
		public void write(String relativePath, MessageFileData fileData) {
			calls.add(new WriteCall(null, relativePath, null));
		}

		@Override
		public Path resolvePath(String namespace, String relativePath) {
			Path root = resolveNamespaceRoot(namespace);
			return root.resolve(relativePath + ".yml").normalize();
		}

		@Override
		public Path resolvePath(String relativePath) {
			return messagesPath.resolve(relativePath + ".yml").normalize();
		}

		private Path resolveNamespaceRoot(String namespace) {
			return namespaceResolver.resolveNamespaceRoot(namespace);
		}
	}
}
