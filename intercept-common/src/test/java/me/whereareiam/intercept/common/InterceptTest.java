package me.whereareiam.intercept.common;

import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Intercept")
class InterceptTest {
	private final List<String> seen = new ArrayList<>();
	private final Logger logger = Logger.getLogger("intercept-test");

	@DisplayName("Starts phase by phase, within a phase in binding order, and stops in reverse")
	@Test
	void startsPhaseByPhase() {
		Intercept intercept = intercept(
				new Part("commands", StartupPhase.CONNECTIONS),
				new Part("settings", StartupPhase.CONFIGURATION),
				new Part("messages", StartupPhase.RUNTIME),
				new Part("languages", StartupPhase.RUNTIME),
				new Part("database", StartupPhase.PERSISTENCE)
		);

		intercept.start();
		intercept.stop();

		assertEquals(List.of(
				"start settings", "start database", "start messages", "start languages", "start commands",
				"stop commands", "stop languages", "stop messages", "stop database", "stop settings"
		), seen);
	}

	@DisplayName("A failure stops the lifecycles started before, and the failed one, and is passed on")
	@Test
	void failureStopsWhatStarted() {
		IllegalStateException failure = new IllegalStateException("database down");
		Intercept intercept = intercept(
				new Part("settings", StartupPhase.CONFIGURATION),
				new Part("database", StartupPhase.PERSISTENCE, failure),
				new Part("messages", StartupPhase.RUNTIME)
		);

		assertSame(failure, assertThrows(IllegalStateException.class, intercept::start));

		assertEquals(List.of("start settings", "start database", "stop database", "stop settings"), seen);
	}

	@DisplayName("A lifecycle that cannot stop does not keep the others from stopping")
	@Test
	void failedStopDoesNotKeepOthersRunning() {
		Part broken = new Part("database", StartupPhase.PERSISTENCE);
		broken.failOnStop = true;
		Intercept intercept = intercept(new Part("settings", StartupPhase.CONFIGURATION), broken);
		intercept.start();

		intercept.stop();

		assertEquals(List.of("start settings", "start database", "stop database", "stop settings"), seen);
	}

	private @NotNull Intercept intercept(@NotNull Part... parts) {
		return new Intercept(new LinkedHashSet<>(List.of(parts)), logger);
	}

	private final class Part implements RuntimeLifecycle {
		private final String name;
		private final StartupPhase phase;
		private final RuntimeException failure;
		private boolean failOnStop;

		private Part(@NotNull String name, @NotNull StartupPhase phase) {
			this(name, phase, null);
		}

		private Part(@NotNull String name, @NotNull StartupPhase phase, RuntimeException failure) {
			this.name = name;
			this.phase = phase;
			this.failure = failure;
		}

		@Override
		public @NotNull StartupPhase phase() {
			return phase;
		}

		@Override
		public void initialize() {
			seen.add("start " + name);
			if (failure != null) throw failure;
		}

		@Override
		public void shutdown() {
			seen.add("stop " + name);
			if (failOnStop) throw new IllegalStateException("cannot stop " + name);
		}
	}
}
