package me.whereareiam.intercept.common.api;

import me.whereareiam.intercept.InterceptAPI;
import me.whereareiam.intercept.common.Intercept;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("Intercept's API in its lifecycle")
class ApiLifecycleTest {
	private final LanguageService languages = mock(LanguageService.class);
	private final DefaultInterceptAPI api = new DefaultInterceptAPI(languages);
	private final List<String> seen = new ArrayList<>();

	@AfterEach
	void unregister() {
		InterceptAPI.unregister();
	}

	@DisplayName("Is registered with the core and ready as Intercept begins handling players")
	@Test
	void registeredWithTheCoreReadyWithConnections() {
		InterceptAPI.whenReady(intercept -> seen.add("ready"));
		Intercept intercept = intercept(new Probe(StartupPhase.INTERCEPTION));

		intercept.start();

		assertEquals(List.of("interception, registered: true", "ready"), seen);
		assertSame(languages, InterceptAPI.get().languages());
	}

	@DisplayName("Is removed when Intercept stops, before the core it depends on")
	@Test
	void removedWhenStopping() {
		Probe core = new Probe(StartupPhase.CATALOG);
		Intercept intercept = intercept(core);
		intercept.start();
		seen.clear();

		intercept.stop();

		assertEquals(List.of("stop catalog, registered: false"), seen);
		assertEquals(Optional.empty(), InterceptAPI.find());
	}

	@DisplayName("Is not left registered when a later phase cannot start")
	@Test
	void failedStartLeavesNothingRegistered() {
		Probe failing = new Probe(StartupPhase.INTERCEPTION);
		failing.failure = new IllegalStateException("no packet library");
		List<String> ready = new ArrayList<>();
		InterceptAPI.whenReady(instance -> ready.add("ready"));

		assertThrows(IllegalStateException.class, intercept(failing)::start);

		assertEquals(Optional.empty(), InterceptAPI.find());
		assertEquals(List.of(), ready);
	}

	private @NotNull Intercept intercept(@NotNull Probe probe) {
		List<RuntimeLifecycle> lifecycles = List.of(new ApiLifecycle(() -> api), new ReadyLifecycle(), probe);
		return new Intercept(new LinkedHashSet<>(lifecycles), Logger.getLogger("intercept-test"));
	}

	private final class Probe implements RuntimeLifecycle {
		private final StartupPhase phase;
		private RuntimeException failure;

		private Probe(@NotNull StartupPhase phase) {
			this.phase = phase;
		}

		@Override
		public @NotNull StartupPhase phase() {
			return phase;
		}

		@Override
		public void initialize() {
			if (failure != null) throw failure;
			seen.add(name() + ", registered: " + InterceptAPI.find().isPresent());
		}

		@Override
		public void shutdown() {
			seen.add("stop " + name() + ", registered: " + InterceptAPI.find().isPresent());
		}

		private @NotNull String name() {
			return phase.name().toLowerCase();
		}
	}
}
