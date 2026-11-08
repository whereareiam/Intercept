package me.whereareiam.intercept.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts Intercept's parts phase by phase and stops them in reverse.
 */
@Singleton
public final class Intercept {
	private final List<Lifecycle> parts;
	private final Logger logger;
	private final Deque<Lifecycle> started = new ArrayDeque<>();

	@Inject
	public Intercept(@NotNull Set<Lifecycle> parts, @Named("intercept") @NotNull Logger logger) {
		this.parts = parts.stream()
				.sorted(Comparator.comparing(Lifecycle::phase))
				.toList();
		this.logger = logger;
	}

	/**
	 * Starts every part. When one fails, the parts already started are stopped and the failure is thrown.
	 *
	 * @throws IllegalStateException when a part cannot start
	 */
	public synchronized void start() {
		for (Lifecycle part : parts) {
			try {
				part.start();
				started.push(part);
			} catch (Exception failure) {
				stop();
				throw new IllegalStateException("Cannot start " + part.getClass().getSimpleName(), failure);
			}
		}
	}

	/**
	 * Stops every started part, the last started first.
	 */
	public synchronized void stop() {
		while (!started.isEmpty()) {
			Lifecycle part = started.pop();
			try {
				part.stop();
			} catch (RuntimeException failure) {
				logger.log(Level.WARNING, "Cannot stop " + part.getClass().getSimpleName(), failure);
			}
		}
	}
}
