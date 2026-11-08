package me.whereareiam.intercept.common.message;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.common.translation.DefaultTranslationService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.message.PublishResult;
import me.whereareiam.intercept.model.message.RuleFile;
import me.whereareiam.intercept.model.replication.ReloadRequested;
import me.whereareiam.intercept.model.replication.RevisionPublished;
import me.whereareiam.intercept.model.shared.SharedChanges;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedRule;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.intercept.persistence.SharedStore;
import me.whereareiam.intercept.replication.ReplicationService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Keeps the shared layer, the shared folders and the database in step. A publication sends the administrator's
 * changes to the database; every instance then loads the new revision, either when replication announces it or when
 * polling finds it.
 */
@Singleton
public final class DefaultMessageService implements MessageService, Lifecycle {
	private final SharedStore store;
	private final ReplicationService replication;
	private final DefaultTranslationService translations;
	private final Provider<Settings> settings;
	private final Logger logger;
	private final SharedFolders folders;
	private final Path localRules;
	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

	private volatile SharedSnapshot snapshot = SharedSnapshot.EMPTY;
	private volatile List<RuleFile> rules = List.of();
	private ScheduledExecutorService poller;

	@Inject
	public DefaultMessageService(
			@NotNull SharedStore store,
			@NotNull ReplicationService replication,
			@NotNull DefaultTranslationService translations,
			@NotNull Provider<Settings> settings,
			@NotNull Configura configura,
			@Named("dataPath") @NotNull Path dataPath,
			@Named("intercept") @NotNull Logger logger
	) {
		this.store = store;
		this.replication = replication;
		this.translations = translations;
		this.settings = settings;
		this.logger = logger;
		this.folders = new SharedFolders(configura, dataPath);
		this.localRules = dataPath.resolve("local").resolve("rules");
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() throws IOException {
		Files.createDirectories(localRules);
		use(store.load());

		replication.subscribe(RevisionPublished.class, published -> refresh(published.getRevision()));
		replication.subscribe(ReloadRequested.class, ignored -> reload());

		long interval = settings.get().getRevisionPoll().toMillis();
		poller = Executors.newSingleThreadScheduledExecutor(task -> Thread.ofPlatform()
				.daemon()
				.name("Intercept revision poll")
				.unstarted(task));
		poller.scheduleWithFixedDelay(() -> refresh(-1), interval, interval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stop() {
		if (poller != null) poller.shutdownNow();
		poller = null;
	}

	@Override
	public synchronized @NotNull PublishResult publish(@NotNull String author) {
		SharedFolders.Pending pending;
		try {
			pending = folders.pending();
		} catch (IOException exception) {
			throw new UncheckedIOException("Cannot read messages/ and rules/", exception);
		}

		SharedChanges changes = changes(pending, author, Instant.now());
		if (changes.isEmpty()) return new PublishResult(snapshot.getRevision(), 0, 0, 0);

		long revision = store.apply(changes);
		try {
			use(store.load(), SharedFolders.Pending.NONE);
		} catch (IOException exception) {
			throw new UncheckedIOException("Published revision " + revision + ", but cannot rewrite messages/", exception);
		}
		replication.publish(new RevisionPublished(revision));

		return new PublishResult(
				revision,
				changes.getMessages().size(),
				changes.getRemovedMessages().size(),
				changes.getRules().size() + changes.getRemovedRules().size()
		);
	}

	@Override
	public synchronized void reload() {
		try {
			use(store.load());
		} catch (IOException exception) {
			throw new UncheckedIOException("Cannot reload the shared messages", exception);
		}
		translations.reload();
	}

	@Override
	public void refresh(long revision) {
		try {
			long known = snapshot.getRevision();
			if (revision >= 0 && revision <= known) return;
			if (revision < 0 && store.revision() <= known) return;

			synchronized (this) {
				SharedSnapshot latest = store.load();
				if (latest.getRevision() > snapshot.getRevision()) use(latest);
			}
		} catch (Exception exception) {
			logger.log(Level.WARNING, "Cannot load the shared messages of revision " + revision, exception);
		}
	}

	@Override
	public long revision() {
		return snapshot.getRevision();
	}

	@Override
	public @NotNull List<RuleFile> rules() {
		return rules;
	}

	@Override
	public void onChange(@NotNull Runnable listener) {
		listeners.add(listener);
	}

	private void use(SharedSnapshot latest) throws IOException {
		use(latest, folders.pending());
	}

	private void use(SharedSnapshot latest, SharedFolders.Pending pending) throws IOException {
		folders.write(latest, pending);
		snapshot = latest;
		translations.useShared(latest);
		rules = ruleFiles(latest);
		if (!pending.isEmpty())
			logger.info("Loaded shared revision " + latest.getRevision() + "; unpublished changes in messages/ and rules/ are kept");

		listeners.forEach(Runnable::run);
	}

	private SharedChanges changes(SharedFolders.Pending pending, String author, Instant now) {
		List<SharedMessage> messages = new ArrayList<>();
		List<SharedChanges.MessageKey> removed = new ArrayList<>();
		pending.removedMessages().forEach(id -> removed.add(new SharedChanges.MessageKey(id.language(), id.key())));

		pending.messages().forEach((id, text) -> {
			// A message set back to the organisation's text is no longer a change of the network's.
			boolean organisationText = translations.organisationText(id.language(), id.key())
					.map(text.text()::equals)
					.orElse(false);
			if (organisationText) {
				if (isShared(id)) removed.add(new SharedChanges.MessageKey(id.language(), id.key()));
				return;
			}
			messages.add(new SharedMessage(id.language(), id.key(), text.file(), text.text(), author, now));
		});

		List<SharedRule> rules = new ArrayList<>();
		for (Map.Entry<String, String> rule : pending.rules().entrySet())
			rules.add(new SharedRule(rule.getKey(), rule.getValue(), author, now));

		return new SharedChanges(messages, removed, rules, pending.removedRules());
	}

	private boolean isShared(SharedFolders.MessageId id) {
		return snapshot.getMessages().stream()
				.anyMatch(message -> message.getLanguage().equals(id.language()) && message.getKey().equals(id.key()));
	}

	private List<RuleFile> ruleFiles(SharedSnapshot latest) throws IOException {
		List<RuleFile> files = new ArrayList<>();
		latest.getRules().forEach(rule -> files.add(new RuleFile(rule.getPath(), rule.getContent(), false)));

		try (Stream<Path> paths = Files.walk(localRules)) {
			for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
				String name = path.getFileName().toString();
				if (!name.endsWith(".yml") && !name.endsWith(".yaml")) continue;

				String relative = localRules.relativize(path).toString().replace('\\', '/');
				files.add(new RuleFile(relative, Files.readString(path, StandardCharsets.UTF_8), true));
			}
		}

		return List.copyOf(files);
	}
}
