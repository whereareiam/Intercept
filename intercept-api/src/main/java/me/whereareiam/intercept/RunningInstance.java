package me.whereareiam.intercept;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the {@link InterceptAPI} of the running Intercept behind the static methods of {@link InterceptAPI}; an
 * interface cannot keep this state itself without exposing it.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class RunningInstance {
	private static final Logger logger = Logger.getLogger(InterceptAPI.class.getName());
	private static final List<Consumer<InterceptAPI>> waiting = new ArrayList<>();
	private static volatile InterceptAPI instance;
	private static volatile boolean ready;

	static @NotNull InterceptAPI get() {
		InterceptAPI current = instance;
		if (current == null)
			throw new IllegalStateException("Intercept is not running. Start after Intercept, or use InterceptAPI.whenReady.");

		return current;
	}

	static @NotNull Optional<InterceptAPI> find() {
		return Optional.ofNullable(instance);
	}

	static void whenReady(@NotNull Consumer<InterceptAPI> action) {
		InterceptAPI current;
		synchronized (waiting) {
			current = instance;
			if (!ready) {
				waiting.add(action);
				return;
			}
		}

		action.accept(current);
	}

	static void register(@NotNull InterceptAPI intercept) {
		synchronized (waiting) {
			if (instance != null) throw new IllegalStateException("Intercept is already registered");

			instance = intercept;
		}
	}

	static void markReady() {
		InterceptAPI current;
		List<Consumer<InterceptAPI>> actions;
		synchronized (waiting) {
			current = instance;
			if (current == null) throw new IllegalStateException("Intercept is not registered");
			if (ready) return;

			ready = true;
			actions = List.copyOf(waiting);
			waiting.clear();
		}

		for (Consumer<InterceptAPI> action : actions)
			run(action, current);
	}

	static void unregister() {
		synchronized (waiting) {
			instance = null;
			ready = false;
		}
	}

	private static void run(@NotNull Consumer<InterceptAPI> action, @NotNull InterceptAPI intercept) {
		try {
			action.accept(intercept);
		} catch (RuntimeException failure) {
			// another plugin's failure is not Intercept's, so it does not stop Intercept from starting
			logger.log(Level.WARNING, "An action waiting for Intercept failed", failure);
		}
	}
}
