package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.whereareiam.intercept.adapter.database.message.DefaultMessagePersistenceService;
import me.whereareiam.intercept.adapter.database.player.DefaultPlayerPersistenceService;
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
import org.jdbi.v3.core.Jdbi;

/**
 * Guice configuration module for database adapter.
 * Provides database-related services and bindings.
 */
public class DatabaseConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(DatabaseService.class).to(DefaultDatabaseService.class).asEagerSingleton();
		bind(Jdbi.class).toProvider(JdbiProvider.class);
		bind(MessagePersistenceService.class).to(DefaultMessagePersistenceService.class);
		bind(PlayerPersistenceService.class).to(DefaultPlayerPersistenceService.class);
		
		// Bridge registers itself as an event listener in constructor
		bind(PlayerDatabaseBridge.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	public MessageFileRepository provideMessageFileRepository(Jdbi jdbi) {
		return jdbi.onDemand(MessageFileRepository.class);
	}

	@Provides
	@Singleton
	public MessageEntryRepository provideMessageEntryRepository(Jdbi jdbi) {
		return jdbi.onDemand(MessageEntryRepository.class);
	}

	@Provides
	@Singleton
	public MessageTranslationRepository provideMessageTranslationRepository(Jdbi jdbi) {
		return jdbi.onDemand(MessageTranslationRepository.class);
	}

	@Provides
	@Singleton
	public MessageRegexPatternRepository provideMessageRegexPatternRepository(Jdbi jdbi) {
		return jdbi.onDemand(MessageRegexPatternRepository.class);
	}

	@Provides
	@Singleton
	public MessageRegexPlaceholderRepository provideMessageRegexPlaceholderRepository(Jdbi jdbi) {
		return jdbi.onDemand(MessageRegexPlaceholderRepository.class);
	}

	@Provides
	@Singleton
	public PlayerRepository providePlayerRepository(Jdbi jdbi) {
		return jdbi.onDemand(PlayerRepository.class);
	}
}