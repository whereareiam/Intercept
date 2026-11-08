package me.whereareiam.intercept.common.message;

import com.fasterxml.jackson.databind.node.ObjectNode;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.common.Folders;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedRule;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.semantica.source.Source;
import me.whereareiam.semantica.source.file.FileSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The shared folders on disk: {@code translations/}, with a folder per language, {@code all/} for messages of every
 * language and {@code rules/}, which an administrator edits, and an untouched copy of the same revision in
 * {@code .cache/shared/}, which tells what the administrator changed.
 */
final class SharedFolders {
	private final Configura configura;
	private final Copy working;
	private final Copy base;

	SharedFolders(@NotNull Configura configura, @NotNull Folders folders) {
		this.configura = configura;
		this.working = new Copy(folders.translations(), folders.rules());

		Path cache = folders.cache("shared");
		this.base = new Copy(cache, cache.resolve("rules"));
	}

	/**
	 * Reads what the administrator changed since the folders were last written.
	 *
	 * @return the changes, empty when the folders match the revision they were written from
	 * @throws IOException when a file cannot be read
	 */
	@NotNull Pending pending() throws IOException {
		return Pending.between(read(base), read(working));
	}

	/**
	 * Writes a revision to both copies, keeping the administrator's unpublished changes in {@code messages/} and
	 * {@code rules/}.
	 *
	 * @param snapshot the revision
	 * @param pending  unpublished changes to keep on top of it
	 * @throws IOException when a file cannot be written
	 */
	void write(@NotNull SharedSnapshot snapshot, @NotNull Pending pending) throws IOException {
		Content revision = Content.of(snapshot);
		write(base, revision);
		write(working, pending.applyTo(revision));
	}

	private Content read(Copy copy) throws IOException {
		Map<MessageId, FileText> messages = new LinkedHashMap<>();
		Source.Content content = FileSource.builder(copy.messages(), configura)
				.skip(Folders.NOT_LANGUAGES.toArray(String[]::new))
				.build()
				.load();
		for (Locale language : content.getLocales()) {
			content.entries(language).forEach((key, entry) -> messages.put(
					new MessageId(language, key),
					new FileText(file(entry.origin(), key), entry.text())
			));
		}

		Map<String, String> rules = new TreeMap<>();
		if (Files.isDirectory(copy.rules())) {
			try (Stream<Path> files = Files.walk(copy.rules())) {
				for (Path file : files.filter(SharedFolders::isYaml).toList())
					rules.put(slashes(copy.rules().relativize(file)), Files.readString(file, StandardCharsets.UTF_8));
			}
		}

		return new Content(messages, rules);
	}

	private void write(Copy copy, Content content) throws IOException {
		clearLanguages(copy.messages());
		clear(copy.rules());

		Map<Path, ObjectNode> files = new TreeMap<>();
		content.messages().forEach((id, text) -> {
			Path file = copy.messages().resolve(folder(id.language())).resolve(text.file());
			ObjectNode node = files.computeIfAbsent(file, ignored -> configura.mapper().createObjectNode());
			node.put(relativeKey(text.file(), id.key()), text.text());
		});
		for (Map.Entry<Path, ObjectNode> file : files.entrySet()) {
			Files.createDirectories(file.getKey().getParent());
			configura.writeNode(file.getKey(), file.getValue());
		}

		for (Map.Entry<String, String> rule : content.rules().entrySet()) {
			Path file = copy.rules().resolve(rule.getKey());
			Files.createDirectories(file.getParent());
			Files.writeString(file, rule.getValue(), StandardCharsets.UTF_8);
		}
	}

	// Messages for every language, such as a plugin's key made of references to neutral keys, live in all/.
	static @NotNull String folder(@NotNull Locale language) {
		return Locale.ROOT.equals(language) ? FileSource.ROOT_FOLDER : language.toLanguageTag();
	}

	// The file's path is the prefix of its keys: "server/format.yml" holds "server.format.prefix" as "prefix".
	static @NotNull String relativeKey(@NotNull String file, @NotNull String key) {
		String prefix = stem(file).replace('/', '.') + ".";
		return key.startsWith(prefix) ? key.substring(prefix.length()) : key;
	}

	static @NotNull String file(@NotNull String origin, @NotNull String key) {
		int slash = origin.indexOf('/');
		if (slash >= 0 && slash < origin.length() - 1) return origin.substring(slash + 1);

		int dot = key.indexOf('.');
		return (dot < 0 ? key : key.substring(0, dot)) + ".yml";
	}

	private static String stem(String file) {
		int dot = file.lastIndexOf('.');
		return dot < 0 ? file : file.substring(0, dot);
	}

	// The messages folder also holds rules/ and local/, which are not languages and are left alone.
	private static void clearLanguages(Path folder) throws IOException {
		if (!Files.isDirectory(folder)) return;

		for (Path language : children(folder)) {
			String name = language.getFileName().toString();
			if (!Files.isDirectory(language) || name.startsWith(".") || Folders.NOT_LANGUAGES.contains(name)) continue;

			clear(language);
			Files.deleteIfExists(language);
		}
	}

	private static List<Path> children(Path folder) throws IOException {
		try (Stream<Path> children = Files.list(folder)) {
			return children.toList();
		}
	}

	private static void clear(Path folder) throws IOException {
		if (!Files.isDirectory(folder)) return;

		try (Stream<Path> paths = Files.walk(folder)) {
			List<Path> files = paths.sorted(Comparator.reverseOrder()).toList();
			for (Path path : files) {
				if (path.equals(folder)) continue;
				if (Files.isDirectory(path) || isTranslationFile(path)) Files.deleteIfExists(path);
			}
		} catch (java.nio.file.DirectoryNotEmptyException ignored) {
			// A folder that still holds a file the administrator put there is kept.
		}
	}

	private static boolean isYaml(Path path) {
		String name = path.getFileName().toString();
		return Files.isRegularFile(path) && (name.endsWith(".yml") || name.endsWith(".yaml"));
	}

	private static boolean isTranslationFile(Path path) {
		String name = path.getFileName().toString();
		return isYaml(path) || name.endsWith(".json") || name.endsWith(".properties");
	}

	private static String slashes(Path relative) {
		List<String> parts = new ArrayList<>();
		for (Path part : relative) parts.add(part.toString());

		return String.join("/", parts);
	}

	private record Copy(Path messages, Path rules) {
	}

	record MessageId(Locale language, String key) {
	}

	record FileText(String file, String text) {
	}

	/**
	 * The shared messages and rule files of one copy.
	 */
	record Content(Map<MessageId, FileText> messages, Map<String, String> rules) {
		static Content of(SharedSnapshot snapshot) {
			Map<MessageId, FileText> messages = new LinkedHashMap<>();
			for (SharedMessage message : snapshot.getMessages())
				messages.put(new MessageId(message.getLanguage(), message.getKey()), new FileText(message.getFile(), message.getText()));

			Map<String, String> rules = new TreeMap<>();
			for (SharedRule rule : snapshot.getRules()) rules.put(rule.getPath(), rule.getContent());

			return new Content(messages, rules);
		}
	}

	/**
	 * What an administrator changed in {@code messages/} and {@code rules/} and has not published.
	 */
	record Pending(
			Map<MessageId, FileText> messages,
			List<MessageId> removedMessages,
			Map<String, String> rules,
			List<String> removedRules
	) {
		static final Pending NONE = new Pending(Map.of(), List.of(), Map.of(), List.of());

		static Pending between(Content base, Content working) {
			Map<MessageId, FileText> messages = new LinkedHashMap<>();
			working.messages().forEach((id, text) -> {
				if (!text.equals(base.messages().get(id))) messages.put(id, text);
			});
			List<MessageId> removedMessages = base.messages().keySet().stream()
					.filter(id -> !working.messages().containsKey(id))
					.toList();

			Map<String, String> rules = new TreeMap<>();
			working.rules().forEach((path, text) -> {
				if (!text.equals(base.rules().get(path))) rules.put(path, text);
			});
			List<String> removedRules = base.rules().keySet().stream()
					.filter(path -> !working.rules().containsKey(path))
					.toList();

			return new Pending(messages, removedMessages, rules, removedRules);
		}

		boolean isEmpty() {
			return messages.isEmpty() && removedMessages.isEmpty() && rules.isEmpty() && removedRules.isEmpty();
		}

		Content applyTo(Content revision) {
			Map<MessageId, FileText> messages = new LinkedHashMap<>(revision.messages());
			removedMessages.forEach(messages::remove);
			messages.putAll(this.messages);

			Map<String, String> rules = new TreeMap<>(revision.rules());
			removedRules.forEach(rules::remove);
			rules.putAll(this.rules);

			return new Content(messages, rules);
		}
	}
}
