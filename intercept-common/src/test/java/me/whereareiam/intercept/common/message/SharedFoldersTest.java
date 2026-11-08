package me.whereareiam.intercept.common.message;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.Folders;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedRule;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedFoldersTest {
	private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");

	@TempDir
	Path dataPath;

	private final Configura configura = Configura.builder().format(Format.YAML).build();

	@Test
	void aRevisionWrittenToTheFoldersHasNothingPending() throws Exception {
		SharedFolders folders = new SharedFolders(configura, new Folders(dataPath));

		folders.write(revision(1, "<gold>Server</gold> » "), SharedFolders.Pending.NONE);

		assertTrue(folders.pending().isEmpty());
		assertTrue(Files.readString(dataPath.resolve("translations/en/server/format.yml")).contains("prefix"));
		assertEquals("pattern: \"x\"\n", Files.readString(dataPath.resolve("translations/rules/essentials.yml")));
	}

	@Test
	void anEditedMessageAndARemovedRuleArePending() throws Exception {
		SharedFolders folders = new SharedFolders(configura, new Folders(dataPath));
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);

		Files.writeString(dataPath.resolve("translations/en/server/format.yml"), "prefix: \"new\"\n");
		Files.writeString(dataPath.resolve("translations/en/essentials.yml"), "motd: \"Hello\"\n");
		Files.delete(dataPath.resolve("translations/rules/essentials.yml"));

		SharedFolders.Pending pending = folders.pending();
		assertEquals("new", pending.messages().get(new SharedFolders.MessageId(Locale.ENGLISH, "server.format.prefix")).text());
		assertEquals("essentials.yml", pending.messages().get(new SharedFolders.MessageId(Locale.ENGLISH, "essentials.motd")).file());
		assertEquals(List.of("essentials.yml"), pending.removedRules());
	}

	@Test
	void anotherServersRevisionKeepsTheUnpublishedChanges() throws Exception {
		SharedFolders folders = new SharedFolders(configura, new Folders(dataPath));
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);
		Files.writeString(dataPath.resolve("translations/en/essentials.yml"), "motd: \"Hello\"\n");

		folders.write(revision(2, "from another server"), folders.pending());

		SharedFolders.Pending pending = folders.pending();
		assertEquals(1, pending.messages().size());
		assertTrue(pending.messages().containsKey(new SharedFolders.MessageId(Locale.ENGLISH, "essentials.motd")));
		assertTrue(Files.readString(dataPath.resolve("translations/en/server/format.yml")).contains("from another server"));
	}

	@Test
	void aMessageForEveryLanguageIsReadFromAndWrittenToAll() throws Exception {
		SharedFolders folders = new SharedFolders(configura, new Folders(dataPath));
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);
		Files.createDirectories(dataPath.resolve("translations/all"));
		Files.writeString(dataPath.resolve("translations/all/identica.yml"), "login:\n  failed: \"{@server.format.prefix}{@game.account.login-failed}\"\n");

		SharedFolders.Pending pending = folders.pending();
		SharedFolders.MessageId id = new SharedFolders.MessageId(Locale.ROOT, "identica.login.failed");
		assertEquals("identica.yml", pending.messages().get(id).file());

		folders.write(revision(2, "old"), pending);
		assertTrue(Files.readString(dataPath.resolve("translations/all/identica.yml")).contains("game.account.login-failed"));
		assertEquals("all", SharedFolders.folder(Locale.ROOT));
	}

	@Test
	void writingARevisionLeavesThisServersOwnFilesAlone() throws Exception {
		Path local = dataPath.resolve("translations/local/en/server/format.yml");
		Files.createDirectories(local.getParent());
		Files.writeString(local, "prefix: \"Event » \"\n");

		SharedFolders folders = new SharedFolders(configura, new Folders(dataPath));
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);
		folders.write(revision(2, "new"), folders.pending());

		assertEquals("prefix: \"Event » \"\n", Files.readString(local));
		assertTrue(folders.pending().isEmpty());
	}

	@Test
	void aKeyIsWrittenRelativeToItsFile() {
		assertEquals("prefix", SharedFolders.relativeKey("server/format.yml", "server.format.prefix"));
		assertEquals("format.prefix", SharedFolders.relativeKey("server.yml", "server.format.prefix"));
		assertEquals("essentials.yml", SharedFolders.file("en", "essentials.motd"));
		assertEquals("server/format.yml", SharedFolders.file("en/server/format.yml", "server.format.prefix"));
	}

	private static SharedSnapshot revision(long revision, String prefix) {
		return new SharedSnapshot(
				revision,
				List.of(new SharedMessage(Locale.ENGLISH, "server.format.prefix", "server/format.yml", prefix, "Alice", NOW)),
				List.of(new SharedRule("essentials.yml", "pattern: \"x\"\n", "Alice", NOW))
		);
	}
}
