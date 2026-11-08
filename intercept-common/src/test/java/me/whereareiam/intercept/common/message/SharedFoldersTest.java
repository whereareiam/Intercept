package me.whereareiam.intercept.common.message;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
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
		SharedFolders folders = new SharedFolders(configura, dataPath);

		folders.write(revision(1, "<gold>Oraylen</gold> » "), SharedFolders.Pending.NONE);

		assertTrue(folders.pending().isEmpty());
		assertTrue(Files.readString(dataPath.resolve("messages/en/oraylen/format.yml")).contains("prefix"));
		assertEquals("pattern: \"x\"\n", Files.readString(dataPath.resolve("rules/essentials.yml")));
	}

	@Test
	void anEditedMessageAndARemovedRuleArePending() throws Exception {
		SharedFolders folders = new SharedFolders(configura, dataPath);
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);

		Files.writeString(dataPath.resolve("messages/en/oraylen/format.yml"), "prefix: \"new\"\n");
		Files.writeString(dataPath.resolve("messages/en/essentials.yml"), "motd: \"Hello\"\n");
		Files.delete(dataPath.resolve("rules/essentials.yml"));

		SharedFolders.Pending pending = folders.pending();
		assertEquals("new", pending.messages().get(new SharedFolders.MessageId(Locale.ENGLISH, "oraylen.format.prefix")).text());
		assertEquals("essentials.yml", pending.messages().get(new SharedFolders.MessageId(Locale.ENGLISH, "essentials.motd")).file());
		assertEquals(List.of("essentials.yml"), pending.removedRules());
	}

	@Test
	void anotherServersRevisionKeepsTheUnpublishedChanges() throws Exception {
		SharedFolders folders = new SharedFolders(configura, dataPath);
		folders.write(revision(1, "old"), SharedFolders.Pending.NONE);
		Files.writeString(dataPath.resolve("messages/en/essentials.yml"), "motd: \"Hello\"\n");

		folders.write(revision(2, "from another server"), folders.pending());

		SharedFolders.Pending pending = folders.pending();
		assertEquals(1, pending.messages().size());
		assertTrue(pending.messages().containsKey(new SharedFolders.MessageId(Locale.ENGLISH, "essentials.motd")));
		assertTrue(Files.readString(dataPath.resolve("messages/en/oraylen/format.yml")).contains("from another server"));
	}

	@Test
	void aKeyIsWrittenRelativeToItsFile() {
		assertEquals("prefix", SharedFolders.relativeKey("oraylen/format.yml", "oraylen.format.prefix"));
		assertEquals("format.prefix", SharedFolders.relativeKey("oraylen.yml", "oraylen.format.prefix"));
		assertEquals("essentials.yml", SharedFolders.file("en", "essentials.motd"));
		assertEquals("oraylen/format.yml", SharedFolders.file("en/oraylen/format.yml", "oraylen.format.prefix"));
	}

	private static SharedSnapshot revision(long revision, String prefix) {
		return new SharedSnapshot(
				revision,
				List.of(new SharedMessage(Locale.ENGLISH, "oraylen.format.prefix", "oraylen/format.yml", prefix, "Alice", NOW)),
				List.of(new SharedRule("essentials.yml", "pattern: \"x\"\n", "Alice", NOW))
		);
	}
}
