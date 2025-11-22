package me.whereareiam.intercept.adapter.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.whereareiam.intercept.adapter.database.message.DefaultMessagePersistenceService;
import me.whereareiam.intercept.adapter.database.provider.JdbiProvider;
import me.whereareiam.intercept.adapter.database.repository.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.MessageTranslationRepository;
import me.whereareiam.intercept.database.DatabaseService;
import me.whereareiam.intercept.database.MessagePersistenceService;
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
}