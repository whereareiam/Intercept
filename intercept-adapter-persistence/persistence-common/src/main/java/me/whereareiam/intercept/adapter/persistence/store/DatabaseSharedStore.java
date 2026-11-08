package me.whereareiam.intercept.adapter.persistence.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.dialectica.Dialectica;
import me.whereareiam.dialectica.adapter.jdbi.entity.EntityAccess;
import me.whereareiam.dialectica.adapter.jdbi.entity.EntityRowMapper;
import me.whereareiam.intercept.adapter.persistence.entity.RevisionEntity;
import me.whereareiam.intercept.adapter.persistence.entity.SharedMessageEntity;
import me.whereareiam.intercept.adapter.persistence.entity.SharedRuleEntity;
import me.whereareiam.intercept.model.shared.SharedChanges;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedRule;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.intercept.persistence.SharedStore;
import me.whereareiam.intercept.persistence.database.DatabaseService;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Keeps what the network shares in three tables. A publication changes rows and raises the revision in one
 * transaction, so an instance that reads the new revision reads all of its rows.
 */
@Singleton
public final class DatabaseSharedStore implements SharedStore {
	private final DatabaseService database;

	@Inject
	public DatabaseSharedStore(@NotNull DatabaseService database) {
		this.database = database;
	}

	@Override
	public @NotNull SharedSnapshot load() {
		return database.jdbi().inTransaction(handle -> {
			long revision = revision(handle);
			List<SharedMessage> messages = handle.createQuery("SELECT * FROM " + SharedMessageEntity.TABLE)
					.map(new EntityRowMapper<>(Dialectica.entity(SharedMessageEntity.class)))
					.stream()
					.map(DatabaseSharedStore::message)
					.toList();
			List<SharedRule> rules = handle.createQuery("SELECT * FROM " + SharedRuleEntity.TABLE)
					.map(new EntityRowMapper<>(Dialectica.entity(SharedRuleEntity.class)))
					.stream()
					.map(DatabaseSharedStore::rule)
					.toList();

			return new SharedSnapshot(revision, messages, rules);
		});
	}

	@Override
	public long revision() {
		return database.jdbi().withHandle(DatabaseSharedStore::revision);
	}

	@Override
	public long apply(@NotNull SharedChanges changes) {
		if (changes.isEmpty()) return revision();

		EntityAccess<SharedMessageEntity> messages = database.access(SharedMessageEntity.class);
		EntityAccess<SharedRuleEntity> rules = database.access(SharedRuleEntity.class);

		return database.jdbi().inTransaction(handle -> {
			// Raising the revision first locks its row, so publications from two servers queue behind each other.
			handle.createUpdate("UPDATE " + RevisionEntity.TABLE + " SET revision = revision + 1, changed_at = :at WHERE name = :name")
					.bind("at", System.currentTimeMillis())
					.bind("name", RevisionEntity.SHARED)
					.execute();

			for (SharedChanges.MessageKey removed : changes.getRemovedMessages())
				messages.delete(removed.getLanguage().toLanguageTag(), removed.getKey());
			for (SharedMessage message : changes.getMessages()) messages.upsert(entity(message));
			for (String removed : changes.getRemovedRules()) rules.delete(removed);
			for (SharedRule rule : changes.getRules()) rules.upsert(entity(rule));

			return revision(handle);
		});
	}

	private static long revision(Handle handle) {
		return handle.createQuery("SELECT revision FROM " + RevisionEntity.TABLE + " WHERE name = :name")
				.bind("name", RevisionEntity.SHARED)
				.mapTo(Long.class)
				.findOne()
				.orElse(0L);
	}

	private static SharedMessage message(SharedMessageEntity entity) {
		return new SharedMessage(
				Locale.forLanguageTag(entity.getLanguage()),
				entity.getMessageKey(),
				entity.getFile(),
				entity.getText(),
				entity.getAuthor(),
				Instant.ofEpochMilli(entity.getChangedAt())
		);
	}

	private static SharedRule rule(SharedRuleEntity entity) {
		return new SharedRule(entity.getPath(), entity.getContent(), entity.getAuthor(), Instant.ofEpochMilli(entity.getChangedAt()));
	}

	private static SharedMessageEntity entity(SharedMessage message) {
		return new SharedMessageEntity(
				message.getLanguage().toLanguageTag(),
				message.getKey(),
				message.getFile(),
				message.getText(),
				message.getAuthor(),
				message.getChangedAt().toEpochMilli()
		);
	}

	private static SharedRuleEntity entity(SharedRule rule) {
		return new SharedRuleEntity(rule.getPath(), rule.getContent(), rule.getAuthor(), rule.getChangedAt().toEpochMilli());
	}
}
