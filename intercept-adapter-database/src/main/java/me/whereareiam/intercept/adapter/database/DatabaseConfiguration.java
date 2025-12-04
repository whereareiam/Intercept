package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.whereareiam.intercept.adapter.database.message.DefaultMessagePersistenceService;
import me.whereareiam.intercept.adapter.database.message.DummyMessagePersistenceService;
import me.whereareiam.intercept.adapter.database.player.DefaultPlayerPersistenceService;
import me.whereareiam.intercept.adapter.database.player.DummyPlayerPersistenceService;
import me.whereareiam.intercept.adapter.database.player.PlayerDatabaseBridge;
import me.whereareiam.intercept.adapter.database.provider.JdbiProvider;
import me.whereareiam.intercept.adapter.database.repository.message.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageRegexPatternRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageRegexPlaceholderRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTranslationRepository;
import me.whereareiam.intercept.adapter.database.repository.player.PlayerRepository;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.database.PlayerPersistenceService;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.model.config.Persistence;
import org.jdbi.v3.core.Jdbi;

/**
 * Guice configuration module for database adapter.
 * Provides database-related services and bindings.
 * Conditionally provides real or dummy implementations based on persistence configuration.
 */
public class DatabaseConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Jdbi.class).toProvider(JdbiProvider.class);
	}

	@Provides
	@Singleton
	public DatabaseService provideDatabaseService(
			Provider<DefaultDatabaseService> realServiceProvider,
			Provider<DummyDatabaseService> dummyServiceProvider,
			Persistence persistence
	) {
		return persistence.isEnabled() ? realServiceProvider.get() : dummyServiceProvider.get();
	}

	@Provides
	@Singleton
	public MessagePersistenceService provideMessagePersistenceService(
			Provider<DefaultMessagePersistenceService> realServiceProvider,
			Provider<DummyMessagePersistenceService> dummyServiceProvider,
			Persistence persistence
	) {
		return persistence.isEnabled() ? realServiceProvider.get() : dummyServiceProvider.get();
	}

	@Provides
	@Singleton
	public PlayerPersistenceService providePlayerPersistenceService(
			Provider<DefaultPlayerPersistenceService> realServiceProvider,
			Provider<DummyPlayerPersistenceService> dummyServiceProvider,
			Persistence persistence
	) {
		return persistence.isEnabled() ? realServiceProvider.get() : dummyServiceProvider.get();
	}

	@Provides
	@Singleton
	public PlayerDatabaseBridge providePlayerDatabaseBridge(
			PlayerPersistenceService persistenceService,
			DatabaseService databaseService,
			EventManager eventManager
	) {
		// Always create the bridge - it checks databaseService.isInitialized() before operations
		return new PlayerDatabaseBridge(persistenceService, databaseService, eventManager);
	}

	@Provides
	@Singleton
	public MessageFileRepository provideMessageFileRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(MessageFileRepository.class) : null;
	}

	@Provides
	@Singleton
	public MessageEntryRepository provideMessageEntryRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(MessageEntryRepository.class) : null;
	}

	@Provides
	@Singleton
	public MessageTranslationRepository provideMessageTranslationRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(MessageTranslationRepository.class) : null;
	}

	@Provides
	@Singleton
	public MessageRegexPatternRepository provideMessageRegexPatternRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(MessageRegexPatternRepository.class) : null;
	}

	@Provides
	@Singleton
	public MessageRegexPlaceholderRepository provideMessageRegexPlaceholderRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(MessageRegexPlaceholderRepository.class) : null;
	}

	@Provides
	@Singleton
	public PlayerRepository providePlayerRepository(Jdbi jdbi, Persistence persistence) {
		return persistence.isEnabled() ? jdbi.onDemand(PlayerRepository.class) : null;
	}
}