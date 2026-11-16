package me.whereareiam.intercept.common.api;

import com.google.inject.Singleton;
import me.whereareiam.intercept.InterceptAPI;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Marks Intercept's API ready as Intercept begins handling players, which runs the actions other plugins left
 * waiting for it.
 */
@Singleton
public final class ReadyLifecycle implements RuntimeLifecycle {
	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.CONNECTIONS;
	}

	@Override
	public void initialize() {
		InterceptAPI.markReady();
	}

	/**
	 * Leaves the readiness to {@link ApiLifecycle}, which removes the API with it.
	 */
	@Override
	public void shutdown() {
	}
}
