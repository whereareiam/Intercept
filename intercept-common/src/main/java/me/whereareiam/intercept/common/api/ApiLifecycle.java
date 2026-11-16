package me.whereareiam.intercept.common.api;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.InterceptAPI;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;
import me.whereareiam.intercept.lifecycle.StartupPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Registers Intercept's API for other plugins as the core starts, and removes it when Intercept stops. The API is
 * marked ready later, by {@link ReadyLifecycle}, once Intercept begins handling players.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class ApiLifecycle implements RuntimeLifecycle {
	private final @NotNull Provider<DefaultInterceptAPI> api;
	private boolean registered;

	@Override
	public @NotNull StartupPhase phase() {
		return StartupPhase.RUNTIME;
	}

	@Override
	public void initialize() {
		InterceptAPI.register(api.get());
		registered = true;
	}

	@Override
	public void shutdown() {
		if (!registered) return;

		registered = false;
		InterceptAPI.unregister();
	}
}
