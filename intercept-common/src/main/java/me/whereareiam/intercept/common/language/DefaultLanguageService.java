package me.whereareiam.intercept.common.language;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.translation.DefaultTranslationService;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.model.replication.LanguageChosen;
import me.whereareiam.intercept.persistence.LanguageStore;
import me.whereareiam.intercept.replication.ReplicationService;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the choices of the players on this instance in memory, stores each choice and replicates it.
 */
@Singleton
public final class DefaultLanguageService implements LanguageService, Lifecycle {
	private final LanguageStore store;
	private final LanguageApplier applier;
	private final ReplicationService replication;
	private final DefaultTranslationService translations;
	private final Map<UUID, Locale> chosen = new ConcurrentHashMap<>();
	private final Set<UUID> loaded = ConcurrentHashMap.newKeySet();

	@Inject
	public DefaultLanguageService(
			@NotNull LanguageStore store,
			@NotNull LanguageApplier applier,
			@NotNull ReplicationService replication,
			@NotNull DefaultTranslationService translations
	) {
		this.store = store;
		this.applier = applier;
		this.replication = replication;
		this.translations = translations;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		replication.subscribe(LanguageChosen.class, this::receive);
	}

	@Override
	public void stop() {
		chosen.clear();
		loaded.clear();
	}

	@Override
	public @NotNull Optional<Locale> chosen(@NotNull UUID uniqueId) {
		return Optional.ofNullable(chosen.get(uniqueId));
	}

	@Override
	public void choose(@NotNull UUID uniqueId, @NotNull Locale language) {
		if (!available().contains(language))
			throw new IllegalArgumentException(language.toLanguageTag() + " is not one of the available languages");

		store.save(uniqueId, language);
		chosen.put(uniqueId, language);
		applier.apply(uniqueId, language);
		replication.publish(new LanguageChosen(uniqueId, language));
	}

	@Override
	public void reset(@NotNull UUID uniqueId) {
		store.delete(uniqueId);
		chosen.remove(uniqueId);
		applier.apply(uniqueId, null);
		replication.publish(new LanguageChosen(uniqueId, null));
	}

	@Override
	public void load(@NotNull UUID uniqueId) {
		loaded.add(uniqueId);
		store.find(uniqueId).ifPresentOrElse(
				language -> chosen.put(uniqueId, language),
				() -> chosen.remove(uniqueId)
		);
	}

	@Override
	public void unload(@NotNull UUID uniqueId) {
		loaded.remove(uniqueId);
		chosen.remove(uniqueId);
	}

	@Override
	public @NotNull Set<Locale> available() {
		return translations.languages();
	}

	// Another instance stored the choice already; this one only follows it for a player who is here.
	private void receive(LanguageChosen message) {
		if (!loaded.contains(message.getUniqueId())) return;

		if (message.getLanguage() == null) chosen.remove(message.getUniqueId());
		else chosen.put(message.getUniqueId(), message.getLanguage());

		applier.apply(message.getUniqueId(), message.getLanguage());
	}
}
