package me.whereareiam.intercept.common.config;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.feature.comment.CommentFeature;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import me.whereareiam.intercept.model.config.Settings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("settings.yml")
class SettingsProviderTest {
	private final Configura configura = Configura.builder()
			.format(Format.YAML)
			.feature(CommentFeature.defaults())
			.build();

	@DisplayName("Is read in the configuration phase, and reading it before fails without touching the file")
	@Test
	void readInTheConfigurationPhase(@TempDir Path dataPath) {
		SettingsProvider settings = new SettingsProvider(configura, dataPath);

		assertEquals(StartupPhase.CONFIGURATION, settings.phase());
		assertThrows(IllegalStateException.class, settings::get);
		assertFalse(Files.exists(dataPath.resolve("settings.yml")));

		settings.initialize();
		Settings read = settings.get();

		assertTrue(Files.exists(dataPath.resolve("settings.yml")));
		assertSame(read, settings.get());
	}
}
