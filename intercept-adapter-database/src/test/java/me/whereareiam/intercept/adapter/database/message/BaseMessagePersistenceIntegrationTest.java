package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageDownloadCoordinator;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageUploadCoordinator;
import me.whereareiam.intercept.adapter.database.repository.message.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageExtensionRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTemplateRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTranslationRepository;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.persistence.TranslationDataService;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

abstract class BaseMessagePersistenceIntegrationTest extends BaseTest {
	protected DefaultMessagePersistenceService postgresService;
	protected DefaultMessagePersistenceService mariaDbService;
	protected MessageFileRepository postgresFileRepo;
	protected MessageFileRepository mariaDbFileRepo;
	protected MessageEntryRepository postgresEntryRepo;
	protected MessageEntryRepository mariaDbEntryRepo;
	protected MessageTranslationRepository postgresTranslationRepo;
	protected MessageTranslationRepository mariaDbTranslationRepo;
	protected MessageTemplateRepository postgresTemplateRepo;
	protected MessageTemplateRepository mariaDbTemplateRepo;
	protected MessageExtensionRepository postgresExtensionRepo;
	protected MessageExtensionRepository mariaDbExtensionRepo;
	protected Path messagesPath;
	protected MessageFormatRegistry formatRegistry;
	protected ReservedKeyRegistry reservedKeyRegistry;
	protected NamespaceResolver namespaceResolver;

	@BeforeEach
	void baseSetUp() throws IOException {
		postgresFileRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbFileRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageFileRepository.class);
		postgresEntryRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageEntryRepository.class);
		mariaDbEntryRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageEntryRepository.class);
		postgresTranslationRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageTranslationRepository.class);
		mariaDbTranslationRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageTranslationRepository.class);
		postgresTemplateRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageTemplateRepository.class);
		mariaDbTemplateRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageTemplateRepository.class);
		postgresExtensionRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageExtensionRepository.class);
		mariaDbExtensionRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageExtensionRepository.class);

		messagesPath = Files.createTempDirectory("messages-test");
		MessageFileWriter postgresWriter = new TestMessageFileWriter(messagesPath);
		MessageFileWriter mariaWriter = new TestMessageFileWriter(messagesPath);

		formatRegistry = TestMessageSupport.createFormatRegistry();
		reservedKeyRegistry = TestMessageSupport.createReservedKeyRegistry();
		Settings settings = new Settings();
		PlatformNamespaceProvider namespaceProvider = () -> Constants.Namespace.INTERNAL;
		namespaceResolver = TestMessageSupport.createNamespaceResolver(settings, namespaceProvider, messagesPath);

		TranslationDataService noopDataService = new NoopTranslationDataService(messagesPath);

		MessageUploadCoordinator postgresUpload = new MessageUploadCoordinator(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				postgresTemplateRepo,
				postgresExtensionRepo,
				formatRegistry,
				reservedKeyRegistry,
				namespaceResolver
		);

		MessageDownloadCoordinator postgresDownload = new MessageDownloadCoordinator(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				postgresTemplateRepo,
				postgresExtensionRepo,
				postgresWriter,
				formatRegistry,
				reservedKeyRegistry,
				namespaceResolver,
				namespaceProvider
		);

		MessageUploadCoordinator mariaUpload = new MessageUploadCoordinator(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				mariaDbTemplateRepo,
				mariaDbExtensionRepo,
				formatRegistry,
				reservedKeyRegistry,
				namespaceResolver
		);

		MessageDownloadCoordinator mariaDownload = new MessageDownloadCoordinator(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				mariaDbTemplateRepo,
				mariaDbExtensionRepo,
				mariaWriter,
				formatRegistry,
				reservedKeyRegistry,
				namespaceResolver,
				namespaceProvider
		);

		postgresService = new DefaultMessagePersistenceService(
				postgresUpload,
				postgresDownload,
				noopDataService,
				getJdbi(DatabaseType.POSTGRES)
		);

		mariaDbService = new DefaultMessagePersistenceService(
				mariaUpload,
				mariaDownload,
				noopDataService,
				getJdbi(DatabaseType.MARIADB)
		);

		clearTables(DatabaseType.POSTGRES);
		clearTables(DatabaseType.MARIADB);
	}

	protected DefaultMessagePersistenceService service(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresService : mariaDbService;
	}

	protected MessageFileRepository fileRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresFileRepo : mariaDbFileRepo;
	}

	protected MessageEntryRepository entryRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresEntryRepo : mariaDbEntryRepo;
	}

	protected MessageTranslationRepository translationRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresTranslationRepo : mariaDbTranslationRepo;
	}

	protected MessageTemplateRepository templateRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresTemplateRepo : mariaDbTemplateRepo;
	}

	protected MessageExtensionRepository extensionRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresExtensionRepo : mariaDbExtensionRepo;
	}

	protected Path resolveFile(String relative) {
		return messagesPath.resolve(relative);
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

	private static class NoopTranslationDataService implements TranslationDataService {
		private final Path messagesPath;

		private NoopTranslationDataService(Path messagesPath) {
			this.messagesPath = messagesPath;
		}

		@Override
		public Map<String, TranslationEntry> getAllEntries() {
			return Collections.emptyMap();
		}

		@Override
		public Map<String, Path> getFilePaths() {
			return Collections.emptyMap();
		}

		@Override
		public MessageSnapshot createSnapshot() {
			return new MessageSnapshot(Collections.emptyMap(), Collections.emptyMap());
		}

		@Override
		public void reload() {}

		@Override
		public void resetStorage() {
			if (messagesPath == null) return;

			try {
				if (Files.notExists(messagesPath)) {
					Files.createDirectories(messagesPath);
					return;
				}

				try (Stream<Path> stream = Files.walk(messagesPath)) {
					stream
							.sorted(Comparator.reverseOrder())
							.filter(path -> !path.equals(messagesPath))
							.forEach(path -> {
								try {
									Files.deleteIfExists(path);
								} catch (IOException e) {
									throw new IllegalStateException("Failed to delete path: " + path, e);
								}
							});
				}
			} catch (IOException e) {
				throw new IllegalStateException("Failed to reset messages directory: " + messagesPath, e);
			}
		}
	}

	private static class TestMessageFileWriter implements MessageFileWriter {
		private final Path messagesPath;

		private TestMessageFileWriter(Path messagesPath) {
			this.messagesPath = messagesPath;
		}

		@Override
		public void write(String relativePath, MessageFileData fileData) {
			Path target = resolvePath(relativePath);
			try {
				Path parent = target.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				Files.writeString(target, "# test file\n");
			} catch (IOException e) {
				throw new IllegalStateException("Failed to write test message file: " + target, e);
			}
		}

		@Override
		public Path resolvePath(String relativePath) {
			String normalized = relativePath.replace('\\', '/');
			return messagesPath.resolve(normalized + ".yml").normalize();
		}
	}
}

