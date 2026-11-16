package me.whereareiam.intercept.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts and stops Intercept: the bound {@link RuntimeLifecycle}s, phase by phase in the order of
 * {@link StartupPhase}, and within a phase in the order they were bound. They stop in the reverse order.
 */
@Singleton
public final class Intercept {
	private final List<RuntimeLifecycle> lifecycles;
	private final Logger logger;
	private final List<RuntimeLifecycle> started = new ArrayList<>();

	@Inject
	public Intercept(@NotNull Set<RuntimeLifecycle> lifecycles, @Named("intercept") @NotNull Logger logger) {
		this.lifecycles = lifecycles.stream()
				.sorted(Comparator.comparing(RuntimeLifecycle::phase))
				.toList();
		this.logger = logger;
	}

	/**
	 * Starts every lifecycle. When one fails, the ones started before it, and the failed one itself, are stopped
	 * in reverse and the failure is passed on.
	 */
	public synchronized void start() {
		try {
			for (RuntimeLifecycle lifecycle : lifecycles) {
				started.add(lifecycle);
				lifecycle.initialize();
			}
		} catch (RuntimeException | Error failure) {
			stop();
			throw failure;
		}
	}

	/**
	 * Stops every started lifecycle, the last started first.
	 */
	public synchronized void stop() {
		for (int i = started.size() - 1; i >= 0; i--) {
			RuntimeLifecycle lifecycle = started.get(i);
			try {
				lifecycle.shutdown();
			} catch (RuntimeException failure) {
				logger.log(Level.WARNING, "Cannot stop " + lifecycle.getClass().getSimpleName(), failure);
			}
		}
		started.clear();
	}
}
