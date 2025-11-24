package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.dialectica.type.DatabaseType;
import me.whereareiam.intercept.adapter.database.BaseTest;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageDownloadCoordinator;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageUploadCoordinator;
import me.whereareiam.intercept.adapter.database.repository.message.*;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
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
	protected MessageRegexPatternRepository postgresPatternRepo;
	protected MessageRegexPatternRepository mariaDbPatternRepo;
	protected MessageRegexPlaceholderRepository postgresPlaceholderRepo;
	protected MessageRegexPlaceholderRepository mariaDbPlaceholderRepo;
	protected Path messagesPath;

	@BeforeEach
	void baseSetUp() throws IOException {
		postgresFileRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageFileRepository.class);
		mariaDbFileRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageFileRepository.class);
		postgresEntryRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageEntryRepository.class);
		mariaDbEntryRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageEntryRepository.class);
		postgresTranslationRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageTranslationRepository.class);
		mariaDbTranslationRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageTranslationRepository.class);
		postgresPatternRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageRegexPatternRepository.class);
		mariaDbPatternRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageRegexPatternRepository.class);
		postgresPlaceholderRepo = getJdbi(DatabaseType.POSTGRES).onDemand(MessageRegexPlaceholderRepository.class);
		mariaDbPlaceholderRepo = getJdbi(DatabaseType.MARIADB).onDemand(MessageRegexPlaceholderRepository.class);

		messagesPath = Files.createTempDirectory("messages-test");
		MessageFileWriter postgresWriter = new TestMessageFileWriter(messagesPath);
		MessageFileWriter mariaWriter = new TestMessageFileWriter(messagesPath);

		MessageDataService noopDataService = new NoopMessageDataService(messagesPath);

		MessageUploadCoordinator postgresUpload = new MessageUploadCoordinator(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				postgresPatternRepo,
				postgresPlaceholderRepo,
				messagesPath
		);

		MessageDownloadCoordinator postgresDownload = new MessageDownloadCoordinator(
				postgresFileRepo,
				postgresEntryRepo,
				postgresTranslationRepo,
				postgresPatternRepo,
				postgresPlaceholderRepo,
				postgresWriter
		);

		MessageUploadCoordinator mariaUpload = new MessageUploadCoordinator(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				mariaDbPatternRepo,
				mariaDbPlaceholderRepo,
				messagesPath
		);

		MessageDownloadCoordinator mariaDownload = new MessageDownloadCoordinator(
				mariaDbFileRepo,
				mariaDbEntryRepo,
				mariaDbTranslationRepo,
				mariaDbPatternRepo,
				mariaDbPlaceholderRepo,
				mariaWriter
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

	protected MessageRegexPatternRepository patternRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresPatternRepo : mariaDbPatternRepo;
	}

	protected MessageRegexPlaceholderRepository placeholderRepo(DatabaseType type) {
		return type == DatabaseType.POSTGRES ? postgresPlaceholderRepo : mariaDbPlaceholderRepo;
	}

	protected Path resolveFile(String relative) {
		return messagesPath.resolve(relative);
	}

	private void clearTables(DatabaseType type) {
		getJdbi(type).useHandle(handle -> {
			handle.execute("DELETE FROM intercept_message_regex_placeholders");
			handle.execute("DELETE FROM intercept_message_regex_patterns");
			handle.execute("DELETE FROM intercept_message_translations");
			handle.execute("DELETE FROM intercept_message_entries");
			handle.execute("DELETE FROM intercept_message_files");
		});
	}

	private static class NoopMessageDataService implements MessageDataService {
		private final Path messagesPath;

		private NoopMessageDataService(Path messagesPath) {
			this.messagesPath = messagesPath;
		}

		@Override
		public void initialize() {}

		@Override
		public Map<String, CompiledMessageEntry> getAllEntries() {
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
		public void write(String relativePath, me.whereareiam.intercept.model.messaging.document.MessageDocument fileData) {
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

